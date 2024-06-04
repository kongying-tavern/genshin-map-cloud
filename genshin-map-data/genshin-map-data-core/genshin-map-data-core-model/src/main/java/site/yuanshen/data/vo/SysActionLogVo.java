package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.util.Map;

/**
* 系统操作日志表;系统操作日志前端封装
*
* @since 2024-06-04 05:15:23
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysActionLog前端封装", description = "系统操作日志表;系统操作日志前端封装")
public class SysActionLogVo {

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
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * IPv4
     */
    @Schema(title = "IPv4")
    private String ipv4;

    /**
     * 设备编码
     */
    @Schema(title = "设备编码")
    private String deviceId;

    /**
     * 操作名
     */
    @Schema(title = "操作名")
    private String action;

    /**
     * 是否是错误
     */
    @Schema(title = "是否是错误")
    private Boolean isError;

    /**
     * JSON对象
     */
    @Schema(title = "JSON对象")
    private Map<String, Object> extraData;

}