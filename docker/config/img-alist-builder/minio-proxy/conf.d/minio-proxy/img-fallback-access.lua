local format_tag = string.upper(ngx.var.origin_ext)
local target_uri, err = img_fallback_chain.run_img_fallback_chain(
    ngx.var.proxy_server,
    ngx.var.proxy_port,
    true,
    ngx.var.base_path,
    ngx.var.origin_ext
)

if err then
    ngx.log(
        ngx.ERR,
        "[" .. format_tag .. " FB-CHAIN] ",
        err
    )
end

ngx.var.target_uri = target_uri
