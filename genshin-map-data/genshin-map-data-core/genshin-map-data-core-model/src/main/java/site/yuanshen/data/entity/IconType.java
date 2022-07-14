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
 * 图标分类表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("icon_type")
@Schema(title = "IconType对象", description = "图标分类表")
public class IconType extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名
     */
    @Schema(title = "分类名")
    @TableField("name")
    private String name;

    /**
     * 父级分类id（-1为根分类）
     */
    @Schema(title = "父级分类id（-1为根分类）")
    @TableField("parent")
    private Long parent;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    @TableField("is_final")
    private Boolean isFinal;


}
