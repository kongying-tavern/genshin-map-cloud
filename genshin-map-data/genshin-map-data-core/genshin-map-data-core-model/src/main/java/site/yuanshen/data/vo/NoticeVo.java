package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

/**
* 消息通知前端封装
*
* @since 2023-05-31 03:12:05
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Notice前端封装", description = "消息通知前端封装")
public class NoticeVo {

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
     * 频道
     */
    @Schema(title = "频道")
    private List<String> channel;

    /**
     * 标题
     */
    @Schema(title = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(title = "内容")
    private String content;

    /**
     * 排序
     */
    @Schema(title = "排序")
    private Integer sortIndex;

    /**
     * 有效期开始时间
     */
    @Schema(title = "有效期开始时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp validTimeStart;

    /**
     * 有效期结束时间
     */
    @Schema(title = "有效期结束时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp validTimeEnd;

}
