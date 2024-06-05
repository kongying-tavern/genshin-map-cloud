package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "操作日志分页查询数据封装", description = "操作日志分页查询数据封装")
public class SysActionLogSearchVo {
    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    /**
     * 用户名
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * IP地址
     */
    @Schema(title = "IPv4")
    private String ipv4;

    /**
     * 昵称
     */
    @Schema(title = "设备ID")
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
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort;
}
