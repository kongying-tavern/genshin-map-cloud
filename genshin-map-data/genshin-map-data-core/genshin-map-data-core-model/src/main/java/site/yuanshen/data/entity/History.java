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

@Schema(title = "history")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "history")
public class History extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "")
    private Long id;

    /**
     * 内容
     */
    @TableField(value = "content")
    @Schema(title = "内容")
    private String content;

    /**
     * md5
     */
    @TableField(value = "md5")
    @Schema(title = "md5")
    private String md5;

    /**
     * 类型id
     */
    @TableField(value = "t_id")
    @Schema(title = "类型id")
    private Long tId;

    /**
     * 记录类型
     */
    @TableField(value = "\"type\"")
    @Schema(title = "记录类型")
    private Integer type;

    /**
     * ipv4
     */
    @TableField(value = "ipv4")
    @Schema(title = "ipv4")
    private String ipv4;

}