package site.yuanshen.data.vo.helper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 带遍历的分页查询VO
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "带分类的分页查询前端封装", description = "带分类的分页查询前端封装")
public class PageAndTypeSearchVo {

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    /**
     * 父级类型ID列表
     */
    @Schema(title = "父级类型ID列表")
    private List<Long> typeIdList;

}
