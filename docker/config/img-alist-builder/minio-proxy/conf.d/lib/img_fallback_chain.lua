local http_lib = require "resty.http"
local dns_resolver = dofile("/etc/nginx/conf.d/lib/dns_resolver.lua")
local http_utils = dofile("/etc/nginx/conf.d/lib/http_utils.lua")

local _IMG_FALLBACK_CHAIN = {}

-- 回退链映射表
local FALLBACK_CHAIN_MAP = {
    png = { "png" },
    jpg = { "png", "jpg" },
    jpeg = { "png", "jpeg" },
    jfif = { "png", "jfif" },
}

function _IMG_FALLBACK_CHAIN.is_method_proxiable(method)
    return method == "GET" or method == "HEAD" or method == "OPTIONS"
end

function _IMG_FALLBACK_CHAIN.is_webp_supported(headers)
    local accept_header = headers["accept"] or ""
    local webp_supported = false

    if accept_header then
        -- 将头部转为小写方便匹配
        local accept_lower = string.lower(accept_header)

        -- 简单解析逻辑：
        -- 1. 检查是否包含 "image/webp"
        -- 2. 检查是否包含 "image/*"
        -- 3. 检查是否包含 "*/*"
        if string.find(accept_lower, "image/webp") then
            webp_supported = true
        elseif string.find(accept_lower, "image/%*") then
            webp_supported = true
        elseif string.find(accept_lower, "%*/%*") then
            webp_supported = true
        end
    end

    return webp_supported
end

function _IMG_FALLBACK_CHAIN.build_fallback_exts(origin_ext, is_webp_supported)
    local ext_map = FALLBACK_CHAIN_MAP[origin_ext] or {}
    local fallback_exts = {}

    -- 复制映射表防止修改原始映射表
    for _, ext in ipairs(ext_map) do
        table.insert(fallback_exts, ext)
    end

    if is_webp_supported then
        table.insert(fallback_exts, 1, "webp")
    end

    return fallback_exts
end

function _IMG_FALLBACK_CHAIN.build_proxy_headers(headers)
    local proxy_headers = {}

    for key, val in pairs(headers) do
        -- 跳过一些不应该透传的 Header
        -- Host: 我们通常希望后端看到真实的 Host 或者由 proxy_pass 自动处理，这里手动构造更灵活
        -- Content-Length: HEAD 请求没有 Body，所以不需要 Content-Length，否则后端可能报错
        -- Connection: 保持连接由 resty.http 内部管理
        if key == "host" or key == "content-length" or key == "connection" then
            goto continue_loop
        end

        -- 注意：Lua table 的 key 默认是小写 (ngx.req.get_headers 的行为)
        -- 如果后端对大小写敏感，可能需要调整，但大多数 Web 服务器不敏感
        proxy_headers[key] = val

        ::continue_loop::
    end
    --  显式设置 Host 头
    proxy_headers["host"] = ngx.var.host

    return proxy_headers
end

function _IMG_FALLBACK_CHAIN.try_images(
    proxy_server,
    proxy_port,
    use_dns_resolver,
    base_path,
    origin_ext,
    fallback_exts,
    proxy_headers
)
    local origin_uri = base_path .. "." .. origin_ext
    local format_tag = string.upper(origin_ext)

    -- 查询 MinIO 服务 IP
    local proxy_ip = nil
    if use_dns_resolver then
        local dns_proxy_ip, err = dns_resolver.resolve(proxy_server)
        if err then
            return origin_uri, "DNS resolution failed for " .. proxy_server .. ": " .. (err or "unknown")
        end
        proxy_ip = dns_proxy_ip
    end

    -- 查找合适的文件
    local target_uri = nil
    local proxy_ip_server = "http://" .. (proxy_ip or proxy_server) .. ":" .. proxy_port
    local httpc = http_lib.new()
    local found = false

    ngx.log(
        ngx.DEBUG,
        "[" .. format_tag .. " FB-Chain] ",
        "Fallback chain for: ", origin_uri,
        " (", table.concat(fallback_exts, " -> "), ")"
    )

    for _, ext in ipairs(fallback_exts) do
        -- 构造地址
        local probe_uri = base_path .. "." .. ext
        local probe_url = proxy_ip_server .. probe_uri

        -- 发起请求
        local res, err = httpc:request_uri(probe_url, {
            method = "HEAD",
            ssl_verify = false,
            headers = proxy_headers,
            keepalive = true
        })

        if err then
            return origin_uri, "Error while probing " .. probe_url .. ": " .. (err or "unknown")
        end

        if res then
            if res.status == 200 then
                found = true
                target_uri = probe_uri

                ngx.log(
                    ngx.DEBUG,
                    "[" .. format_tag .. " FB-Chain] ",
                    "Matched format: ", ext
                )
                break
            else
                ngx.log(
                    ngx.DEBUG,
                    "[" .. format_tag .. " FB-Chain] ",
                    "Probe failed for: ", origin_uri, ",",
                    "Probe URI: ", probe_uri,
                    " (status: ", res.status, ")"
                )
            end
        end
    end

    if not found then
        return origin_uri, "No image found for: " .. origin_uri
    end

    return target_uri or origin_uri
end

function _IMG_FALLBACK_CHAIN.run_img_fallback_chain(
    proxy_server,
    proxy_port,
    use_dns_resolver,
    base_path,
    origin_ext
)
    local format_tag = string.upper(origin_ext)
    local origin_uri = base_path .. "." .. origin_ext

    -- 1. 判断方法类型，针对指定类型执行类型探测
    local req_method = http_utils.get_req_method()
    if not _IMG_FALLBACK_CHAIN.is_method_proxiable(req_method) then
        -- 如果是 POST, PUT, DELETE 等，直接跳过 Lua 逻辑
        -- Nginx 将继续使用默认的 $target_uri (即原始 $uri) 进行 proxy_pass
        ngx.log(
            ngx.DEBUG,
            "[" .. format_tag .. " FB-CHAIN] ",
            "Non-GET-like request (", req_method, "), skipping format probe."
        )
        return origin_uri
    end

    -- 2. 解析 Accept 头，判断是否支持 WebP
    local req_headers = http_utils.get_req_headers()
    local supports_webp = _IMG_FALLBACK_CHAIN.is_webp_supported(req_headers)

    -- 3. 构建扩展名列表
    local fallback_exts = _IMG_FALLBACK_CHAIN.build_fallback_exts(origin_ext, supports_webp)

    -- 4. 构造请求头
    local proxy_headers = _IMG_FALLBACK_CHAIN.build_proxy_headers(req_headers)

    -- 5. 获取地址
    local target_uri, err = _IMG_FALLBACK_CHAIN.try_images(
        proxy_server,
        proxy_port,
        use_dns_resolver,
        base_path,
        origin_ext,
        fallback_exts,
        proxy_headers
    )

    return target_uri, err
end

return _IMG_FALLBACK_CHAIN
