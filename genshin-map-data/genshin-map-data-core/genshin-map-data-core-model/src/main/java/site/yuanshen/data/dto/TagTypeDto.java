package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.TagType;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.TagTypeVo;
import java.time.LocalDateTime;


/**
 * 图标标签分类数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TagType数据封装", description = "图标标签分类表数据封装")
public class TagTypeDto {

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
    private SysUserVo creator;

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
    private SysUserVo updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 分类名称
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

    public TagTypeDto(TagType tagType) {
        BeanUtils.copy(tagType, this);
    }

    public TagTypeDto(TagTypeVo tagTypeVo) {
        BeanUtils.copy(tagTypeVo, this);
    }

    @JSONField(serialize = false)
    public TagType getEntity() {
        return BeanUtils.copy(this, TagType.class);
    }

    @JSONField(serialize = false)
    public TagTypeVo getVo() {
        return BeanUtils.copy(this, TagTypeVo.class);
    }

}