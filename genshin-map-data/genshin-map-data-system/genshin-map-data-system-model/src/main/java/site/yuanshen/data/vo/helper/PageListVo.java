package site.yuanshen.data.vo.helper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分页记录前端封装
 *
 * @author Moment
 * @since 2022-06-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "分页记录前端封装", description = "分页记录前端封装")
public class PageListVo<T> {

    /**
     * 查询数据列表
     */
    List<T> record;

    /**
     * 总数
     */
    private long total = 0;

    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

}
