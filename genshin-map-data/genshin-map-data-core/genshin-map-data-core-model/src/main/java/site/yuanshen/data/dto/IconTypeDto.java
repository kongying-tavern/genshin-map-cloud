package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.SysUserVo;

import java.time.LocalDateTime;


/**
 * 图标分类数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "IconType数据封装", description = "图标分类表数据封装")
public class IconTypeDto {

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
     * 创建人人信息
     */
    private SysUserSmallVo creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新人信息
     */
    private SysUserSmallVo updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 分类名
     */
    private String name;

    /**
     * 父级分类ID（-1为根分类）
     */
    private Long parent;

    /**
     * 是否为末端类型
     */
    private Boolean isFinal;

    public IconTypeDto(IconType iconType) {
        BeanUtils.copy(iconType, this);
    }

    public IconTypeDto(IconTypeVo iconTypeVo) {
        BeanUtils.copy(iconTypeVo, this);
    }

    @JSONField(serialize = false)
    public IconType getEntity() {
        return BeanUtils.copy(this, IconType.class);
    }

    @JSONField(serialize = false)
    public IconTypeVo getVo() {
        return BeanUtils.copy(this, IconTypeVo.class);
    }

}