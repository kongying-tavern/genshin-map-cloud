package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.vo.IconVo;

import java.util.List;

/**
 * 图标数据对象
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Icon数据对象", description = "图标数据对象")
public class IconDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 图标ID
     */
    @Schema(title = "图标ID")
    private Long iconId;

    /**
     * 图标名称
     */
    @Schema(title = "图标名称")
    private String name;

    /**
     * 图标类型ID列表
     */
    @Schema(title = "图标类型ID列表")
    private List<Long> typeIdList;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    private String url;

    /**
     * 创建者ID
     */
    @Schema(title = "创建者ID")
    private Long creator;

    public IconDto(Icon icon) {
        CachedBeanCopier.copyProperties(icon, this);
    }

    public IconDto(IconVo iconVo) {
        CachedBeanCopier.copyProperties(iconVo, this);
    }

    public Icon getEntity() {
        return CachedBeanCopier.copyProperties(this, Icon.class);
    }

    public IconVo getVo() {
        return CachedBeanCopier.copyProperties(this, IconVo.class);
    }

}
