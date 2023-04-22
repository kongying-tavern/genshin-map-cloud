package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.vo.TagVo;
import java.time.LocalDateTime;


/**
 * 图标标签主表路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Tag数据封装", description = "图标标签主表数据封装")
public class TagDto {

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
     * 标签名
     */
    private String tag;

    /**
     * 图标ID
     */
    private Long iconId;

    public TagDto(Tag tag) {
        BeanUtils.copy(tag, this);
    }

    public TagDto(TagVo tagVo) {
        BeanUtils.copy(tagVo, this);
    }

    @JSONField(serialize = false)
    public Tag getEntity() {
        return BeanUtils.copy(this, Tag.class);
    }

    @JSONField(serialize = false)
    public TagVo getVo() {
        return BeanUtils.copy(this, TagVo.class);
    }

}