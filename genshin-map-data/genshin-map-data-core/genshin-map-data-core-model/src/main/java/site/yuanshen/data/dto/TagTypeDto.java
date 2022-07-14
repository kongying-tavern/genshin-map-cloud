package site.yuanshen.data.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.TagType;
import site.yuanshen.data.vo.TagTypeVo;

/**
 * 图标标签分类数据封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "TagType数据封装", description = "图标标签分类数据封装")
public class TagTypeDto {

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

    public TagTypeDto(TagType tagType) {
        CachedBeanCopier.copyProperties(tagType, this);
        this.id = tagType.getId();
    }

    public TagTypeDto(TagTypeVo tagTypeVo) {
        CachedBeanCopier.copyProperties(tagTypeVo, this);
    }

    public TagType getEntity() {
        return CachedBeanCopier.copyProperties(this, TagType.class).setId(this.id);
    }

    public TagTypeVo getVo() {
        return CachedBeanCopier.copyProperties(this, TagTypeVo.class);
    }

}
