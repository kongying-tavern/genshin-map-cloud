package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.ItemType;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.SysUserVo;

import java.time.LocalDateTime;


/**
 * 物品类型数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ItemType数据封装", description = "物品类型表数据封装")
public class ItemTypeDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 图标标签
     */
    private String iconTag;

    /**
     * 类型名
     */
    private String name;

    /**
     * 类型补充说明
     */
    private String content;

    /**
     * 父级类型ID（无父级则为-1）
     */
    private Long parentId;

    /**
     * 是否为末端类型
     */
    private Boolean isFinal;

    /**
     * 隐藏标记
     */
    private Integer hiddenFlag;

    /**
     * 排序
     */
    private Integer sortIndex;

    public ItemTypeDto(ItemType itemType) {
        BeanUtils.copy(itemType, this);
    }

    public ItemTypeDto(ItemTypeVo itemTypeVo) {
        BeanUtils.copy(itemTypeVo, this);
    }

    @JSONField(serialize = false)
    public ItemType getEntity() {
        return BeanUtils.copy(this, ItemType.class);
    }

    @JSONField(serialize = false)
    public ItemTypeVo getVo() {
        return BeanUtils.copy(this, ItemTypeVo.class);
    }

}