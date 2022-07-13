package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.vo.TagVo;

import java.util.List;

/**
 * 图标标签数据封装
 *
 * @author Moment
 * @since 2022-06-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "Tag数据封装", description = "图标标签数据封装")
public class TagDto {

    /**
     * 标签名
     */
    @Schema(title = "标签名")
    private String tag;

    /**
     * 标签类型ID列表
     */
    @Schema(title = "标签类型ID列表")
    private List<Long> typeIdList;

    /**
     * 图标ID
     */
    @Schema(title = "图标ID")
    private Long iconId;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    private String url;

    public TagDto(Tag tag) {
        CachedBeanCopier.copyProperties(tag, this);
    }

    public TagDto(TagVo tagVo) {
        CachedBeanCopier.copyProperties(tagVo, this);
    }

    public Tag getEntity() {
        return CachedBeanCopier.copyProperties(this, Tag.class);
    }

    public TagVo getVo() {
        return CachedBeanCopier.copyProperties(this, TagVo.class);
    }
}
