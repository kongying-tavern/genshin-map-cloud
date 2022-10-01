package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 地区前端封装
 *
 * @author Moment
 * @since 2022-06-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "Area前端封装", description = "地区前端封装")
public class AreaVo {

    /**
     * 乐观锁：修改次数
     */
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 地区ID
     */
    @Schema(title = "地区ID")
    private Long areaId;

    /**
     * 地区名称
     */
    @Schema(title = "地区名称")
    private String name;

    /**
     * 地区代码;
     */
    @Schema(title = "地区代码")
    private String code;

    /**
     * 地区说明
     */
    @Schema(title = "地区说明")
    private String content;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 父级地区ID（无父级则为-1）
     */
    @Schema(title = "父级地区ID（无父级则为-1）")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端地区")
    private Boolean isFinal;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 地区排序
     */
    @Schema(title = "地区排序")
    private Integer sortIndex;
}
