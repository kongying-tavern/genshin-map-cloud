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

    /**
     * 获取测试点位，默认为false不获取，为true时只获取测试点位
     */
    @Schema(title = "获取测试点位，默认为false不获取，为true时只获取测试点位")
    private Boolean getBeta = false;


    /**
     * 是否为测试打点员
     */
    @Schema(title = "是否为测试打点员")
    private Boolean isTestUser;

}
