package site.yuanshen.data.vo.adapter.score;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScoreDataPackVo<T> {
    String scope = "";

    String span = "";

    Long userId = 0L;

    T data;
}
