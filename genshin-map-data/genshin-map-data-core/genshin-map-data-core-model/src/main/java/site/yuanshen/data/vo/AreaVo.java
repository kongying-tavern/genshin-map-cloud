package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;

/**
* 地区主表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Area前端封装", description = "地区主表前端封装")
public class AreaVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 地区名称
     */
    @Schema(title = "地区名称")
    private String name;

    /**
     * 地区代码
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
     * 是否为末端地区
     */
    @Schema(title = "是否为末端地区")
    private Boolean isFinal;

    /**
     * 权限屏蔽标记
     */
    @Schema(title = "权限屏蔽标记")
    private Integer hiddenFlag;

    /**
     * 额外标记;低位第一位：前台是否显示
     */
    @Schema(title = "额外标记;低位第一位：前台是否显示")
    private Integer specialFlag;

    /**
     * 排序
     */
    @Schema(title = "排序")
    private Integer sortIndex;

}
