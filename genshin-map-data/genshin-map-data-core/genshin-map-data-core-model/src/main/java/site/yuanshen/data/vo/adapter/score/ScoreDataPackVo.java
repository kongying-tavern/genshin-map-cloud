package site.yuanshen.data.vo.adapter.score;

import lombok.Data;
import lombok.experimental.Accessors;
import site.yuanshen.data.vo.SysUserSmallVo;

@Data
@Accessors(chain = true)
public class ScoreDataPackVo<T> {
    String scope = "";

    String span = "";

    Long userId = 0L;

    SysUserSmallVo user = new SysUserSmallVo();

    T data;
}
