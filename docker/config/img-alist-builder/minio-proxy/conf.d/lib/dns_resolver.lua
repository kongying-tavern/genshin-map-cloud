local resolver_lib = require "resty.dns.resolver"

local _DNS_RESOLVER = {}

-- DNS 服务器
local DNS_SERVER = "127.0.0.11"
local DNS_PORT = 53
-- 缓存时间 (秒)，避免频繁查询
local CACHE_TTL = 600
-- 初始化共享字典 (需要在 nginx.conf 的 http 块中定义，见下文说明)
-- 如果没定义，这里会尝试动态获取或报错，建议先在 nginx.conf 定义好
local CACHE_NAME = "dns_cache"

-- 解析域名并返回 IP
-- @param hostname string 域名
-- @return string|nil ip 地址，如果失败返回 nil 和 错误信息
-- @return string|nil err 错误信息
function _DNS_RESOLVER.resolve(hostname)
    ngx.log(ngx.DEBUG, "[DNS Resolver] Resolving " .. (hostname or ""))
    if not hostname then
        return nil, "Hostname is required"
    end

    -- 1. 尝试从缓存获取
    local cache = ngx.shared[CACHE_NAME]
    if cache then
        local cached_ip = cache:get(hostname)
        if cached_ip then
            return cached_ip
        end
    end

    -- 2. 缓存未命中，发起 DNS 查询
    -- 2.1 创建 DNS 解析器
    local r, err = resolver_lib:new {
        nameservers = {
            { DNS_SERVER, DNS_PORT }
        },
        retrans = 2,
        timeout = 2000,
        no_random = true,
    }

    if not r then
        return nil, "Failed to init dns resolver: " .. (err or "unknown")
    end

    -- 2.2 查询 DNS 解析
    local answers, err = r:query(hostname, nil, {})

    if not answers then
        return nil, "DNS query failed: " .. (err or "unknown")
    end

    if answers.errcode then
        return nil, "DNS error: " .. (answers.errstr or answers.errcode)
    end

    -- 3. 提取 IPv4 地址
    local ip = nil
    for _, ans in ipairs(answers) do
        -- 1 = A record
        if ans.type == 1 then
            ip = ans.address
            break
        end
    end

    if not ip then
        return nil, "No A record found for " .. hostname
    end

    -- 4. 写入缓存
    if cache then
        -- 使用 DNS 返回的 TTL，但不超过最大限制，也不低于最小限制
        local ttl = answers[1].ttl or CACHE_TTL
        if ttl > CACHE_TTL then ttl = CACHE_TTL end
        cache:set(hostname, ip, ttl)
    end

    return ip
end

return _DNS_RESOLVER
