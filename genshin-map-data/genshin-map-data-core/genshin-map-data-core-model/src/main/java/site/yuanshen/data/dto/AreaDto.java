package site.yuanshen.data.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.vo.AreaVo;

/**
 * 地区数据对象
 *
 * @author Moment
 * @since 2022-06-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Area数据对象", description = "地区数据对象")
public class AreaDto {

    /**
     * 地区ID
     */
    @Schema(title = "地区ID")
    private Long areaId;

    /**
     * 地区名称
     */
    @Schema(title = "地区名称")
    private String name;

    /**
     * 地区说明
     */
    @Schema(title = "地区说明")
    private String content;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 父级地区ID（无父级则为-1）
     */
    @Schema(title = "父级地区ID（无父级则为-1）")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端地区")
    private Boolean isFinal;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 地区排序
     */
    @Schema(title = "地区排序")
    private Integer sortIndex;

    public AreaDto(AreaVo areaVo) {
        CachedBeanCopier.copyProperties(areaVo, this);
    }

    public AreaDto(Area area) {
        CachedBeanCopier.copyProperties(area, this);
        this.areaId = area.getId();
    }

    public Area getEntity() {
        return CachedBeanCopier.copyProperties(this, Area.class).setId(this.areaId);
    }

    public AreaVo getVo() {
        return CachedBeanCopier.copyProperties(this, AreaVo.class);
    }

}
