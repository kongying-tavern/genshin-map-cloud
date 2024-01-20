package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物品查询前端封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "物品查询前端封装", description = "物品查询前端封装")
public class ItemSearchVo {

    /**
     * 末端物品类型ID列表
     */
    @Schema(title = "末端物品类型ID列表")
    private List<Long> typeIdList;

    /**
     * 末端地区ID列表
     */
    @Schema(title = "末端地区ID列表")
    private List<Long> areaIdList;

    /**
     * 图标名
     */
    @Schema(title = "物品名")
    private String name;

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
