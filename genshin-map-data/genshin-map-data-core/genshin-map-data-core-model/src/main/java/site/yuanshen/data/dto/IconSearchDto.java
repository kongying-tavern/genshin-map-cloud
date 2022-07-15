package site.yuanshen.data.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.vo.IconSearchVo;

import java.util.List;

/**
 * 图标分页查询数据封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "图标分页查询数据封装", description = "图标分页查询数据封装")
public class IconSearchDto {

    /**
     * 图标ID列表
     */
    @Schema(title = "图标ID列表")
    private List<Long> iconIdList;

    /**
     * 创建者ID
     */
    @Schema(title = "创建者ID")
    private String creator;

    /**
     * 图标分类列表
     */
    @Schema(title = "图标分类列表")
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

    public IconSearchDto(IconSearchVo iconSearchVo) {
        BeanUtils.copyProperties(iconSearchVo, this);
    }

    public Page<Icon> getPageEntity() {
        return new Page<>(current, size);
    }

}
