package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MBPJsonObjectTypeHandler;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 系统操作日志表;系统操作日志
 *
 * @since 2024-06-04 05:15:23
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_action_log", autoResultMap = true)
public class SysActionLog extends BaseEntity {

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
     * IPv4
     */
    @TableField("ipv4")
    private String ipv4;

    /**
     * 设备编码
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 操作名
     */
    @TableField("action")
    private String action;

    /**
     * 是否是错误
     */
    @TableField("is_error")
    private Boolean isError;

    /**
     * 附加信息
     */
    @TableField(value = "extra_data", typeHandler = MBPJsonObjectTypeHandler.class)
    private Map<String, Object> extraData;

}
