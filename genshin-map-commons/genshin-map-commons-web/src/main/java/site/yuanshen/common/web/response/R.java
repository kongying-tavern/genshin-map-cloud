package site.yuanshen.common.web.response;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口统一返回对象
 *
 * @author Moment
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class R<T> implements Serializable {
    /**
     * 是否异常
     */
    private Boolean error;

    /**
     * 响应码
     */
    private Integer errorStatus;

    /**
     * 错误数据
     */
    private Object errorData;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 数据部分
     */
    private T data;

    private LocalDateTime time;

    public R(Codes code, String message, Object error, T data) {
        this.error = code.getIsError();
        this.errorStatus = code.getStatus();
        this.errorData = error;
        this.message = code.getMsg();
        if(StrUtil.isNotBlank(message)) {
            this.message = message;
        }
        this.data = data;
        this.time = LocalDateTime.now();
    }

    public R(Throwable e, String message, Object error, T data) {
        this.error = true;
        this.errorStatus = Codes.FAIL.getStatus();
        this.errorData = error;
        this.message = e.getMessage();
        if(StrUtil.isNotBlank(message)) {
            this.message = message;
        }
        this.data = data;
        this.time = LocalDateTime.now();
    }
}
