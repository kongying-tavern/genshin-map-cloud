package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 图标标签分类前端封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "TagType前端封装", description = "图标标签分类前端封装")
public class TagTypeVo {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 分类ID
     */
    @Schema(title = "分类ID")
    private Long id;

    /**
     * 分类名
     */
    @Schema(title = "分类名")
    private String name;

    /**
     * 父级分类ID（-1为根分类）
     */
    @Schema(title = "父级分类ID（-1为根分类）")
    private Long parent;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    private Boolean isFinal;

}
