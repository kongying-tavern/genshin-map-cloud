package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.vo.ItemVo;

import java.util.List;

/**
 * 物品数据封装
 *
 * @author Moment
 * @since 2022-06-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Item数据封装", description = "物品数据封装")
public class ItemDto {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

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
     * 物品类型ID列表
     */
    @Schema(title = "物品类型ID列表")
    private List<Long> typeIdList;

    /**
     * 地区ID（须确保是末端地区）
     */
    @Schema(title = "地区ID（须确保是末端地区）")
    private Long areaId;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    @Schema(title = "默认描述模板;用于提交新物品点位时的描述模板")
    private String defaultContent;

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
     * 刷新时间
     */
    @Schema(title = "刷新时间(单位:毫秒)")
    private Long defaultRefreshTime;

    /**
     * 物品排序
     */
    @Schema(title = "物品排序")
    private Integer sortIndex;

    /**
     * 默认物品数量
     */
    @Schema(title = "默认物品数量")
    private Integer defaultCount;

    /**
     * 查询条件下物品总数
     */
    @Schema(title = "查询条件下物品总数")
    private Integer count;


    public ItemDto(Item item) {
        CachedBeanCopier.copyProperties(item, this);
        this.itemId = item.getId();
    }

    public ItemDto(ItemVo itemVo) {
        CachedBeanCopier.copyProperties(itemVo, this);
    }

    public Item getEntity() {
        return CachedBeanCopier.copyProperties(this, Item.class).setId(this.itemId);
    }

    public ItemVo getVo() {
        return CachedBeanCopier.copyProperties(this, ItemVo.class);
    }

}
