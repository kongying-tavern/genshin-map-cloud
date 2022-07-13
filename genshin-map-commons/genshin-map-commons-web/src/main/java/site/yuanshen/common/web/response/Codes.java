package site.yuanshen.common.web.response;

import lombok.Getter;


/**
 * 接口响应的响应码美剧
 *
 * @author Moment
 */
@Getter
public enum Codes {

    //相关的枚举对象
    SUCCESS(200, "成功"),
    FAIL(500, "服务器异常"),
    PARAMETER_ERROR(501, "参数校验异常"),
    FLOW_ERROR(429, "流量限制"),
    DEGRADE_ERROR(430, "服务降级");

    private final Integer code;
    private final String msg;

    Codes(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}