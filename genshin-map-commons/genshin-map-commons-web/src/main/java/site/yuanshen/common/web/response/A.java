package site.yuanshen.common.web.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class A<T> implements Serializable {
    private String action = "";
    private T data;
}
