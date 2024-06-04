package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图标标签分页查询前端封装
 *
 * @author Moment
 * @since 2022-06-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "图标标签分页查询前端封装", description = "图标标签分页查询前端封装")
public class TagSearchVo {

    /**
     * 标签名列表
     */
    @Schema(title = "标签名列表")
    private List<String> tagList;

    /**
     * 图标标签分类列表
     */
    @Schema(title = " 图标标签分类列表")
    private List<Long> typeIdList;

    /**
     * 当前页，从1开始
     */
    @Schema(title = "当前页，从1开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

}
