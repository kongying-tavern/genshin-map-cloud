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
 * 点位额外字段表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("marker_extra")
@Schema(title = "MarkerExtra对象", description = "点位额外字段表")
public class MarkerExtra extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点位id
     */
    @Schema(title = "点位id")
    @TableField("marker_id")
    private Long markerId;

    /**
     * 额外特殊字段具体内容
     */
    @Schema(title = "额外特殊字段具体内容")
    @TableField("marker_extra_content")
    private String markerExtraContent;

    /**
     * 父点位id
     */
    @Schema(title = "父点位id")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 关联其他点位flag
     */
    @Schema(title = "关联其他点位flag")
    @TableField("is_related")
    private Boolean isRelated;


}
