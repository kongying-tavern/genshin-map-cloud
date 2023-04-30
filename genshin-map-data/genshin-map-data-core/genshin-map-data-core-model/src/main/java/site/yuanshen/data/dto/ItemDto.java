package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.vo.ItemVo;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 物品数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Item数据封装", description = "物品表数据封装")
public class ItemDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 物品名称
     */
    private String name;

    /**
     * 地区ID（须确保是末端地区）
     */
    private Long areaId;

    /**
     * 默认刷新时间;单位:毫秒
     */
    private Long defaultRefreshTime;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    private String defaultContent;

    /**
     * 默认数量
     */
    private Integer defaultCount;

    /**
     * 图标标签
     */
    private String iconTag;

    /**
     * 图标样式类型
     */
    private Integer iconStyleType;

    /**
     * 隐藏标志
     */
    private Integer hiddenFlag;

    /**
     * 物品排序
     */
    private Integer sortIndex;

    /**
     * 特殊物品标记;二进制表示；低位第一位：前台是否显示
     */
    private Integer specialFlag;

    /**
     * 物品类型ID列表
     */
    private List<Long> typeIdList;

    public ItemDto(Item item) {
        BeanUtils.copy(item, this);
    }

    public ItemDto(ItemVo itemVo) {
        BeanUtils.copy(itemVo, this);
    }

    @JSONField(serialize = false)
    public Item getEntity() {
        return BeanUtils.copy(this, Item.class);
    }

    @JSONField(serialize = false)
    public ItemVo getVo() {
        return BeanUtils.copy(this, ItemVo.class);
    }

}