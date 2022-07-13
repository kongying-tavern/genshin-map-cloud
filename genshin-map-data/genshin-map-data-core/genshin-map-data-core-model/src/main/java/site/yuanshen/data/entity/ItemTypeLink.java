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
 * 物品-类型关联表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("item_type_link")
@Schema(title = "ItemTypeLink对象", description = "物品-类型关联表")
public class ItemTypeLink extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型id;此处必须为末端类型
     */
    @Schema(title = "类型id;此处必须为末端类型")
    @TableField("type_id")
    private Long typeId;

    /**
     * 物品id
     */
    @Schema(title = "物品id")
    @TableField("item_id")
    private Long itemId;


}
