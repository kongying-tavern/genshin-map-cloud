package site.yuanshen.common.web.response;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class W<T> implements Serializable {
    /**
     * 事件
     */
    private String event = "";

    /**
     * 响应信息
     */
    private String message = "";

    /**
     * 数据部分
     */
    private T data;

    private LocalDateTime time;

    public W(String event, T data, String message) {
        if(StrUtil.isNotBlank(message)) {
            this.message = message;
        }
        this.event = event;
        this.data = data;
        this.time = LocalDateTime.now();
    }
}
