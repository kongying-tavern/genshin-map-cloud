package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerExtra;
import site.yuanshen.data.vo.MarkerExtraVo;

/**
 * 点位额外信息的数据封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Marker的Extra数据封装", description = "点位额外信息的数据封装")
public class MarkerExtraDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 点位ID
     */
    @Schema(title = "点位ID")
    private Long markerId;

    /**
     * 额外特殊字段具体内容
     */
    @Schema(title = "额外特殊字段具体内容")
    private String markerExtraContent;

    /**
     * 父点位ID
     */
    @Schema(title = "父点位ID")
    private Long parentId;

    /**
     * 关联其他点位Flag
     */
    @Schema(title = "关联其他点位Flag")
    private Integer isRelated;

    public MarkerExtraDto(MarkerExtraVo markerExtraVo) {
        BeanUtils.copyProperties(markerExtraVo, this);
    }

    public MarkerExtraDto(MarkerExtra markerExtra) {
        BeanUtils.copyProperties(markerExtra, this);
    }

    public MarkerExtra getEntity() {
        if (markerExtraContent == null || markerExtraContent.equals("")) markerExtraContent = "{}";
        return BeanUtils.copyProperties(this, MarkerExtra.class).setIsRelated(isRelated != null && isRelated.equals(1));
    }

    public MarkerExtraVo getVo() {
        return BeanUtils.copyProperties(this, MarkerExtraVo.class);
    }

}
