package site.yuanshen.data.dto.helper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.helper.PageAndTypeListVo;

import java.util.List;

/**
 * 带分类的分页查询数据封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "带分类的分页查询DTO", description = "带分类的分页查询数据封装")
public class PageAndTypeListDto {

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

    /**
     * 父级类型ID列表
     */
    @Schema(title = "父级类型ID列表")
    private List<Long> typeIdList;

    public <T> Page<T> getPageEntity() {
        return new Page<>(current, size);
    }

    public PageAndTypeListDto(PageAndTypeListVo searchVo) {
        BeanUtils.copyProperties(searchVo, this);
    }

}
