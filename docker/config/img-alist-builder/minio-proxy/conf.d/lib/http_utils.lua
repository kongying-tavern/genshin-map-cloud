local _HTTP_UTILS = {}

function _HTTP_UTILS.get_req_method()
    return ngx.req.get_method() or ""
end

function _HTTP_UTILS.get_req_headers()
    return ngx.req.get_headers()
end

return _HTTP_UTILS
