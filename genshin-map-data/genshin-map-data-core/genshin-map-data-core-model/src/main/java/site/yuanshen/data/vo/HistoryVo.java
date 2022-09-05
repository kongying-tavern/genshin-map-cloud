package site.yuanshen.data.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "History前端封装", description = "历史记录前端封装")
public class HistoryVo {
    @Schema(title = "")
    private Long id;

    /**
     * 内容
     */
    @Schema(title = "内容")
    private Object content;

    /**
     * md5
     */
    @Schema(title = "md5")
    private String md5;

    /**
     * 类型id
     */
    @Schema(title = "类型id")
    private Long tId;

    /**
     * 记录类型
     */
    @Schema(title = "记录类型")
    private Integer type;

    /**
     * ipv4
     */
    @Schema(title = "ipv4")
    private String ipv4;

    /**
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private LocalDateTime createTime;
}
