package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 用户列表查询前端封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "用户列表查询前端封装", description = "用户列表查询前端封装")
public class SysUserDeviceSearchVo {
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
     * 设备状态
     */
    @Schema(title = "设备状态")
    private Integer status;

    /**
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort;

}
