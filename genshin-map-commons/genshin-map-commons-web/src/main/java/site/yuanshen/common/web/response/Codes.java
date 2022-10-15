package site.yuanshen.common.web.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 接口响应的响应码美剧
 *
 * @author Moment
 */
@Getter
@RequiredArgsConstructor
public enum Codes {

    //相关的枚举对象
    SUCCESS(200, "成功", false),
    FAIL(500, "服务器异常", true),
    UNAUTHORIZED(401, "请登录", true),
    PARAMETER_ERROR(501, "参数校验异常", true),
    FLOW_ERROR(429, "流量限制", true),
    DEGRADE_ERROR(430, "服务降级", true);

    private final Integer status;
    private final String msg;
    private final Boolean isError;
}