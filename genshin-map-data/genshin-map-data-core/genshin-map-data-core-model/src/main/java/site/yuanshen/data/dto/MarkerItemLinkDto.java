package site.yuanshen.data.dto;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.vo.MarkerItemLinkVo;

/**
 * 点位-物品关联数据模型
 *
 * @author Moment
 * @since 2022-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "MarkerItemLink数据模型", description = "点位-物品关联数据模型")
public class MarkerItemLinkDto {

    /**
     * 物品id
     */
    @Schema(title = "物品id")
    private Long itemId;

    /**
     * 点位物品数量
     */
    @Schema(title = "点位物品数量")
    private Integer count;

    public MarkerItemLinkDto(MarkerItemLink markerItemLink) {
        BeanUtils.copyProperties(markerItemLink, this);
    }

    public MarkerItemLinkDto(MarkerItemLinkVo markerItemLinkVo) {
        BeanUtils.copyProperties(markerItemLinkVo, this);
    }

    @JSONField(serialize = false)
    public MarkerItemLink getEntity() {
        return BeanUtils.copyProperties(this, MarkerItemLink.class);
    }

    @JSONField(serialize = false)
    public MarkerItemLinkVo getVo() {
        return BeanUtils.copyProperties(this, MarkerItemLinkVo.class);
    }

}
