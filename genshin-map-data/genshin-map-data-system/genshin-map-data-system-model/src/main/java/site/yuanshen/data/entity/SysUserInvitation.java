package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MBPJsonArrayTypeHandler;

import java.sql.Timestamp;
import java.util.List;

/**
 * 系统用户邀请表;系统用户邀请
 *
 * @since 2025-03-26 07:46:02
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_invitation", autoResultMap = true)
public class SysUserInvitation extends BaseEntity {

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
     * 邀请码
     */
    @TableField("code")
    private String code;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Integer roleId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 权限策略
     */
    @TableField(value = "access_policy", typeHandler = MBPJsonArrayTypeHandler.class)
    private List<String> accessPolicy;

}
