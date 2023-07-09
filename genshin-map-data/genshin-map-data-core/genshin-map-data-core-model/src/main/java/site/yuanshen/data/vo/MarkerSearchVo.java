package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点位查询前端封装
 *
 * @author Moment
 * @since 2022-06-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "点位查询前端封装", description = "点位查询前端封装")
public class MarkerSearchVo {

    /**
     * 地区ID列表
     */
    @Schema(title = "地区ID列表")
    private List<Long> areaIdList;

    /**
     * 物品ID列表
     */
    @Schema(title = "物品ID列表")
    private List<Long> itemIdList;

    /**
     * 类型ID列表
     */
    @Schema(title = "类型ID列表")
    private List<Long> typeIdList;

}
