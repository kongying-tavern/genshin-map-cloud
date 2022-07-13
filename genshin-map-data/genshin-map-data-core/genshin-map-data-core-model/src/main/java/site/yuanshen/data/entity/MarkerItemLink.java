package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.BaseEntity;

/**
 * 点位-物品关联表
 *
 * @author Moment
 * @since 2022-07-01 08:25:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("marker_item_link")
@Schema(title = "MarkerItemLink对象", description = "点位-物品关联表")
public class MarkerItemLink extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物品id
     */
    @Schema(title = "物品id")
    @TableField("item_id")
    private Long itemId;

    /**
     * 点位id
     */
    @Schema(title = "点位id")
    @TableField("marker_id")
    private Long markerId;

    /**
     * 点位是否在该物品处计数
     */
    @Schema(title = "点位物品数量")
    @TableField("count")
    private Integer count;


}
