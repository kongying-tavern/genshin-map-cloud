package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.vo.ItemAreaPublicVo;
import java.time.LocalDateTime;


/**
 * 地区公用物品记录表路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ItemAreaPublic数据封装", description = "地区公用物品记录表数据封装")
public class ItemAreaPublicDto {

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
     * 物品ID
     */
    private Long itemId;

    public ItemAreaPublicDto(ItemAreaPublic itemAreaPublic) {
        BeanUtils.copy(itemAreaPublic, this);
    }

    public ItemAreaPublicDto(ItemAreaPublicVo itemAreaPublicVo) {
        BeanUtils.copy(itemAreaPublicVo, this);
    }

    @JSONField(serialize = false)
    public ItemAreaPublic getEntity() {
        return BeanUtils.copy(this, ItemAreaPublic.class);
    }

    @JSONField(serialize = false)
    public ItemAreaPublicVo getVo() {
        return BeanUtils.copy(this, ItemAreaPublicVo.class);
    }

}