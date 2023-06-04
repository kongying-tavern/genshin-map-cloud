package site.yuanshen.data.dto;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.Route;
import site.yuanshen.data.vo.RouteSearchVo;

/**
 * 路线分页查询数据封装
 *
 * @author Moment
 * @since 2023-01-03 05:10:07
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(title = "路线分页查询数据封装", description = "路线分页查询数据封装")
public class RouteSearchDto {

    /**
     * 路线名称模糊搜索字段
     */
    @Schema(title = "路线名称模糊搜索字段")
    private String namePart;

    /**
     * 创建人昵称模糊搜索字段，此字段不能与创建人id字段共存
     */
    @Schema(title = "创建人昵称模糊搜索字段，此字段不能与创建人id字段共存")
    private String creatorNicknamePart;

    /**
     * 创建人id，此字段不能与昵称模糊搜索字段共存
     */
    @Schema(title = "创建人id，此字段不能与昵称模糊搜索字段共存")
    private String creatorId;

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

    public RouteSearchDto(RouteSearchVo searchVo) {
        BeanUtils.copy(searchVo, this);
    }

    public Page<Route> getPage() {
        return new Page<>(current, size);
    }

    public void checkParams() {
        if (StrUtil.hasBlank(this.creatorId,this.creatorNicknamePart))
            throw new RuntimeException("创建人id不能与创建人昵称模糊搜索字段共存");
    }
}
