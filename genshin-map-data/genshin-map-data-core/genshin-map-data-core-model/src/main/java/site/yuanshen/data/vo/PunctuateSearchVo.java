package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 打点查询前端封装
 *
 * @author Moment
 * @since 2022-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "打点查询前端封装", description = "打点查询前端封装")
public class PunctuateSearchVo {

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
     * 提交者ID列表
     */
    @Schema(title = "提交者ID列表")
    private List<Long> authorList;
}
