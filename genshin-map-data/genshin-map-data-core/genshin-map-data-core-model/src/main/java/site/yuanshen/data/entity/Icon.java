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
 * 图标主表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("icon")
@Schema(title = "Icon对象", description = "图标主表")
public class Icon extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 图标id
     */
    @Schema(title = "图标id")
    @TableField("icon_id")
    private Long iconId;

    /**
     * 图标名称
     */
    @Schema(title = "图标名称")
    @TableField("name")
    private String name;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    @TableField("url")
    private String url;

    /**
     * 创建者id
     */
    @Schema(title = "创建者id")
    @TableField("creator")
    private Long creator;


}
