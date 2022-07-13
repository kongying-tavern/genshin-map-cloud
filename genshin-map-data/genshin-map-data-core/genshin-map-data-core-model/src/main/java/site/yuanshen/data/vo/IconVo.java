package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图标前端封装
 *
 * @author Moment
 * @since 2022-06-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "Icon前端封装", description = "Icon前端封装")
public class IconVo {

    /**
     * 图标ID
     */
    @Schema(title = "图标ID")
    private Long iconId;

    /**
     * 图标名称
     */
    @Schema(title = "图标名称")
    private String name;

    /**
     * 图标类型ID列表
     */
    @Schema(title = "图标类型ID列表")
    private List<Long> typeIdList;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    private String url;

    /**
     * 创建者ID
     */
    @Schema(title = "创建者ID")
    private Long creator;

}
