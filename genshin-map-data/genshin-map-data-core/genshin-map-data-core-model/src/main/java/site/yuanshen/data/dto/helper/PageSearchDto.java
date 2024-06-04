package site.yuanshen.data.dto.helper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.helper.PageSearchVo;

/**
 * 分页查询数据封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "分页查询数据封装", description = "分页查询数据封装")
public class PageSearchDto {

    /**
     * 当前页，从1开始
     */
    @Schema(title = "当前页，从1开始")
    private Long current = 0L;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size= 10L;

    public PageSearchDto(PageSearchVo pageSearchVo) {
        BeanUtils.copyNotNull(pageSearchVo, this);
    }

    public <T> Page<T> getPageEntity() {
        return new Page<>(current, size);
    }
}
