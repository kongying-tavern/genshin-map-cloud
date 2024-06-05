package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.ClientUtils;
import site.yuanshen.data.enums.DeviceStatusEnum;

import java.sql.Timestamp;

/**
* 用户设备表;用户设备前端封装
*
* @since 2024-05-08 08:38:30
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserDevice前端封装", description = "用户设备表;用户设备前端封装")
public class SysUserDeviceVo {

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
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 设备编码
     */
    @Schema(title = "设备编码")
    private String deviceId;

    /**
     * IPv4
     */
    @Schema(title = "IPv4")
    private String ipv4;

    /**
     * IP地区
     */
    @Schema(title = "IP地区信息")
    private ClientUtils.Region ipRegion;

    /**
     * 设备状态
     */
    @Schema(title = "设备状态", format = "integer")
    private DeviceStatusEnum status;

    /**
     * 上次登录时间
     */
    @Schema(title = "上次登录时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp lastLoginTime;

}
