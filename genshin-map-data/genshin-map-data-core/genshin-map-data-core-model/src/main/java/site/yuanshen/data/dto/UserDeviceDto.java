package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.UserDevice;
import site.yuanshen.data.vo.UserDeviceVo;
import java.time.LocalDateTime;


/**
 * 用户设备数据封装
 *
 * @since 2024-05-07 12:00:09
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "UserDevice数据封装", description = "用户设备数据封装")
public class UserDeviceDto {

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
    private LocalDateTime updateTime;

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

    public UserDeviceDto(UserDevice userDevice) {
        BeanUtils.copy(userDevice, this);
    }

    public UserDeviceDto(UserDeviceVo userDeviceVo) {
        BeanUtils.copy(userDeviceVo, this);
    }

    @JSONField(serialize = false)
    public UserDevice getEntity() {
        return BeanUtils.copy(this, UserDevice.class);
    }

    @JSONField(serialize = false)
    public UserDeviceVo getVo() {
        return BeanUtils.copy(this, UserDeviceVo.class);
    }

}