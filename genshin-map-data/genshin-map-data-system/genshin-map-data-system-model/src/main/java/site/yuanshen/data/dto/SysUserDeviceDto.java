package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUserDevice;
import site.yuanshen.data.enums.DeviceStatusEnum;
import site.yuanshen.data.vo.SysUserDeviceVo;

import java.sql.Timestamp;


/**
 * 用户设备表;用户设备数据封装
 *
 * @since 2024-05-08 08:38:30
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserDevice数据封装", description = "用户设备表;用户设备数据封装")
public class SysUserDeviceDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设备编码
     */
    private String deviceId;

    /**
     * IPv4
     */
    private String ipv4;

    /**
     * 设备状态
     */
    private DeviceStatusEnum status;

    /**
     * 上次登录时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp lastLoginTime;

    public SysUserDeviceDto(SysUserDevice sysUserDevice) {
        BeanUtils.copy(sysUserDevice, this);
    }

    public SysUserDeviceDto(SysUserDeviceVo sysUserDeviceVo) {
        BeanUtils.copy(sysUserDeviceVo, this);
    }

    @JSONField(serialize = false)
    public SysUserDevice getEntity() {
        return BeanUtils.copy(this, SysUserDevice.class);
    }

    @JSONField(serialize = false)
    public SysUserDeviceVo getVo() {
        return BeanUtils.copy(this, SysUserDeviceVo.class);
    }

}
