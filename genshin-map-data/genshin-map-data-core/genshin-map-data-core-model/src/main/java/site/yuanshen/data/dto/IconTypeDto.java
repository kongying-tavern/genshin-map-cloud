package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.vo.IconTypeVo;

/**
 * 图标分类数据封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "IconType数据封装", description = "图标分类数据封装")
public class IconTypeDto {

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

    public IconTypeDto(IconType iconType) {
        CachedBeanCopier.copyProperties(iconType, this);
        this.id = iconType.getId();
    }

    public IconTypeDto(IconTypeVo iconTypeVo) {
        CachedBeanCopier.copyProperties(iconTypeVo, this);
    }

    public IconType getEntity() {
        return CachedBeanCopier.copyProperties(this, IconType.class).setId(this.id);
    }

    public IconTypeVo getVo() {
        return CachedBeanCopier.copyProperties(this, IconTypeVo.class);
    }
}
