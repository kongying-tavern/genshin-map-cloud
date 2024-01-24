package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;

/**
* 图标标签分类表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TagType前端封装", description = "图标标签分类表前端封装")
public class TagTypeVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 分类名称
     */
    @Schema(title = "分类名称")
    private String name;

    /**
     * 父级分类ID（-1为根分类）
     */
    @Schema(title = "父级分类ID（-1为根分类）")
    private Long parent;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    private Boolean isFinal;

}
