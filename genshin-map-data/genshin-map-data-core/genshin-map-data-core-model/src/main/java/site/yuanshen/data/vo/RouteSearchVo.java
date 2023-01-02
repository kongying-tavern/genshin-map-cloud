package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 路线分页查询前端封装
 *
 * @author Moment
 * @since 2023-01-03 05:10:07
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(name = "路线分页查询前端封装", description = "路线分页查询前端封装")
public class RouteSearchVo {

    /**
     * 路线名称模糊搜索字段
     */
    @Schema(name = "路线名称模糊搜索字段")
    private String namePart;

    /**
     * 创建人昵称模糊搜索字段，此字段不能与创建人id字段共存
     */
    @Schema(name = "创建人昵称模糊搜索字段，此字段不能与创建人id字段共存")
    private String creatorNicknamePart;

    /**
     * 创建人id，此字段不能与昵称模糊搜索字段共存
     */
    @Schema(name = "创建人id，此字段不能与昵称模糊搜索字段共存")
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
}
