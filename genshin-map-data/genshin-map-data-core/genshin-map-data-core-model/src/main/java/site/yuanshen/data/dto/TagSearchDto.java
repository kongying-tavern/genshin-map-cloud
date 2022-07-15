package site.yuanshen.data.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.vo.TagSearchVo;

import java.util.List;

/**
 * 图标标签分页查询数据封装
 *
 * @author Moment
 * @since 2022-06-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "图标标签分页查询数据封装", description = "图标标签分页查询数据封装")
public class TagSearchDto {

    /**
     * 标签名列表
     */
    @Schema(title = "标签名列表")
    private List<String> tagList;

    /**
     * 图标标签分类列表
     */
    @Schema(title = " 图标标签分类列表")
    private List<Long> typeIdList;

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    public Page<Tag> getPageEntity() {
        return new Page<>(current, size);
    }

    public TagSearchDto(TagSearchVo searchVo) {
        CachedBeanCopier.copyProperties(searchVo, this);
    }

}
