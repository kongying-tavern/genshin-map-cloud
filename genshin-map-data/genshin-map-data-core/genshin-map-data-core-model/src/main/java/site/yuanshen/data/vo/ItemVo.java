package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 物品表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Item前端封装", description = "物品表前端封装")
public class ItemVo {

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
     * 物品名称
     */
    @Schema(title = "物品名称")
    private String name;

    /**
     * 地区ID（须确保是末端地区）
     */
    @Schema(title = "地区ID（须确保是末端地区）")
    private Long areaId;

    /**
     * 默认刷新时间;单位:毫秒
     */
    @Schema(title = "默认刷新时间;单位:毫秒")
    private Long defaultRefreshTime;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    @Schema(title = "默认描述模板;用于提交新物品点位时的描述模板")
    private String defaultContent;

    /**
     * 默认数量
     */
    @Schema(title = "默认数量")
    private Integer defaultCount;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 图标样式类型
     */
    @Schema(title = "图标样式类型")
    private Integer iconStyleType;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 物品排序
     */
    @Schema(title = "物品排序")
    private Integer sortIndex;

    /**
     * 特殊物品标记;二进制表示；低位第一位：前台是否显示
     */
    @Schema(title = "特殊物品标记;二进制表示；低位第一位：前台是否显示")
    private Integer specialFlag;

    /**
     * 物品类型ID列表
     */
    @Schema(title = "物品类型ID列表")
    private List<Long> typeIdList;

    /**
     * 查询条件下物品总数
     */
    @Schema(title = "查询条件下物品总数")
    private Integer count;

    /**
     * 物品总数区分
     */
    @Schema(title = "物品总数区分")
    Map<Integer, Integer> countSplit = new HashMap<>();


}
