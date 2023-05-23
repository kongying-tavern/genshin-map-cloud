package site.yuanshen.data.vo.adapter.score;

import lombok.Data;
import lombok.experimental.Accessors;
import site.yuanshen.data.vo.SysUserVo;

@Data
@Accessors(chain = true)
public class ScoreDataPackVo<T> {
    String scope = "";

    String span = "";

    Long userId = 0L;

    SysUserVo user = new SysUserVo();

    T data;
}
