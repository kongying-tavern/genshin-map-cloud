package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 物品类型表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ItemType前端封装", description = "物品类型表前端封装")
public class ItemTypeVo {

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
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 类型名
     */
    @Schema(title = "类型名")
    private String name;

    /**
     * 类型补充说明
     */
    @Schema(title = "类型补充说明")
    private String content;

    /**
     * 父级类型ID（无父级则为-1）
     */
    @Schema(title = "父级类型ID（无父级则为-1）")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    private Boolean isFinal;

    /**
     * 隐藏标记
     */
    @Schema(title = "隐藏标记")
    private Integer hiddenFlag;

    /**
     * 排序
     */
    @Schema(title = "排序")
    private Integer sortIndex;

}