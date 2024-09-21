package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import site.yuanshen.data.enums.HistoryEditType;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "历史记录列表查询前端封装", description = "历史记录列表查询前端封装")
public class HistorySearchVo {
    /**
     * 当前页，从1开始
     */
    @Schema(title = "当前页，从1开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    /**
     * 记录类型,不传时默认查询全部类型
     */
    @Schema(title = "记录类型")
    private Integer type;

    /**
     * 记录类型,不传时默认查询全部数据
     */
    @Schema(title = "类型ID(配合记录类型使用)")
    private List<Long> id;

    /**
     * 操作数据类型
     */
    @Schema(title = "操作数据类型")
    private HistoryEditType editType;

    /**
     * 创建人ID
     */
    @Schema(title = "创建人ID")
    private Long creatorId;

    /**
     * 创建时间开始时间
     */
    @Schema(title = "创建时间开始时间")
    private Timestamp createTimeStart;

    /**
     * 创建时间结束时间
     */
    @Schema(title = "创建时间结束时间")
    private Timestamp createTimeEnd;

    /**
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort;
}
