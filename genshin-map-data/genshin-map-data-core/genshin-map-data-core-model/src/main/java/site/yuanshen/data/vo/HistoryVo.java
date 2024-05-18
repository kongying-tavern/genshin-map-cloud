package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.data.enums.HistoryEditType;

import java.sql.Timestamp;

/**
* 历史操作表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "History前端封装", description = "历史操作表前端封装")
public class HistoryVo {

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
     * 内容
     */
    @Schema(title = "内容")
    private String content;

    /**
     * MD5
     */
    @Schema(title = "MD5")
    private String md5;

    /**
     * 原ID
     */
    @Schema(title = "原ID")
    private Long tId;

    /**
     * 操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签
     */
    @Schema(title = "操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签")
    private Integer type;

    /**
     * IPv4
     */
    @Schema(title = "IPv4")
    private String ipv4;

    /**
     * 修改类型
     */
    @Schema(title = "修改类型", format = "integer")
    private HistoryEditType editType;

}
