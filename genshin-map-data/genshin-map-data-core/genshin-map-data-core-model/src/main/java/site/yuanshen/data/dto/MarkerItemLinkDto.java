package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import java.time.LocalDateTime;


/**
 * 点位-物品关联表路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarkerItemLink数据封装", description = "点位-物品关联表数据封装")
public class MarkerItemLinkDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 点位ID
     */
    private Long markerId;

    /**
     * 物品于该点位数量
     */
    private Integer count;

    public MarkerItemLinkDto(MarkerItemLink markerItemLink) {
        BeanUtils.copy(markerItemLink, this);
    }

    public MarkerItemLinkDto(MarkerItemLinkVo markerItemLinkVo) {
        BeanUtils.copy(markerItemLinkVo, this);
    }

    @JSONField(serialize = false)
    public MarkerItemLink getEntity() {
        return BeanUtils.copy(this, MarkerItemLink.class);
    }

    @JSONField(serialize = false)
    public MarkerItemLinkVo getVo() {
        return BeanUtils.copy(this, MarkerItemLinkVo.class);
    }

}