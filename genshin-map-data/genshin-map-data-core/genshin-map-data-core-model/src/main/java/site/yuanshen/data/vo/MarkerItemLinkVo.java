package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 点位-物品关联前端模型
 *
 * @author Moment
 * @since 2022-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "MarkerItemLink前端模型", description = "点位-物品关联前端模型")
public class MarkerItemLinkVo {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 物品id
     */
    @Schema(title = "物品id")
    private Long itemId;

    /**
     * 点位是否在该物品处计数
     */
    @Schema(title = "点位物品数量")
    private Integer count;


}
