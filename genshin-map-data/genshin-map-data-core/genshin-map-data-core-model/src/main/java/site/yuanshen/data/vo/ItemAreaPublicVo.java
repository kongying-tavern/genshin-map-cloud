package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
* 地区公用物品记录表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ItemAreaPublic前端封装", description = "地区公用物品记录表前端封装")
public class ItemAreaPublicVo {

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
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 物品ID
     */
    @Schema(title = "物品ID")
    private Long itemId;

    /**
     * 物品名称
     */
    @Schema(title = "物品名称")
    private String name;

    /**
     * 地区ID（须确保是末端地区）
     */
    @Schema(title = "地区ID（须确保是末端地区）")
    private Long areaId;

    /**
     * 默认刷新时间;单位:毫秒
     */
    @Schema(title = "默认刷新时间;单位:毫秒")
    private Long defaultRefreshTime;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    @Schema(title = "默认描述模板;用于提交新物品点位时的描述模板")
    private String defaultContent;

    /**
     * 默认数量
     */
    @Schema(title = "默认数量")
    private Integer defaultCount;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 图标样式类型
     */
    @Schema(title = "图标样式类型")
    private Integer iconStyleType;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 物品排序
     */
    @Schema(title = "物品排序")
    private Integer sortIndex;

    /**
     * 特殊物品标记;二进制表示；低位第一位：前台是否显示
     */
    @Schema(title = "特殊物品标记;二进制表示；低位第一位：前台是否显示")
    private Integer specialFlag;

    /**
     * 物品类型ID列表
     */
    @Schema(title = "物品类型ID列表")
    private List<Long> typeIdList;

}