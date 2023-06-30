package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

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
     * 创建人信息
     */
    @Schema(title = "创建人信息")
    private SysUserSmallVo creator;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新人信息
     */
    @Schema(title = "更新人信息")
    private SysUserSmallVo updater;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

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