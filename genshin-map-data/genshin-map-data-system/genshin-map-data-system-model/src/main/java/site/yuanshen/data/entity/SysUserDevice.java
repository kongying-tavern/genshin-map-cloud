package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.enums.DeviceStatusEnum;

import java.sql.Timestamp;

/**
 * 用户设备表;用户设备
 *
 * @since 2024-05-07 02:36:49
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_device", autoResultMap = true)
public class SysUserDevice extends BaseEntity {

    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    @Version
    private Long version;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 更新人
     */
    @TableField(value = "updater_id", fill = FieldFill.INSERT_UPDATE)
    private Long updaterId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备编码
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * IPv4
     */
    @TableField("ipv4")
    private String ipv4;

    /**
     * 设备状态
     */
    @TableField(value = "status", typeHandler = MybatisEnumTypeHandler.class)
    private DeviceStatusEnum status;

    /**
     * 上次登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp lastLoginTime;

}
