package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图标标签前端封装
 *
 * @author Moment
 * @since 2022-06-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "Tag前端封装", description = "图标标签前端封装")
public class TagVo {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 标签名
     */
    @Schema(title = "标签名")
    private String tag;

    /**
     * 标签类型ID列表
     */
    @Schema(title = "标签类型ID列表")
    private List<Long> typeIdList;

    /**
     * 图标ID
     */
    @Schema(title = "图标ID")
    private Long iconId;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    private String url;

}
