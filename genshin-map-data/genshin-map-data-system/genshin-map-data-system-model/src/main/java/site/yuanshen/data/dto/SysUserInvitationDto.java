package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUserInvitation;
import site.yuanshen.data.vo.SysUserInvitationVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import java.util.List;


/**
 * 系统用户邀请表;系统用户邀请数据封装
 *
 * @since 2025-03-26 07:46:02
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserInvitation数据封装", description = "系统用户邀请表;系统用户邀请数据封装")
public class SysUserInvitationDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

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
     * 邀请码
     */
    private String code;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 权限策略
     */
    private List<String> accessPolicy;

    public SysUserInvitationDto(SysUserInvitation sysUserInvitation) {
        BeanUtils.copy(sysUserInvitation, this);
    }

    public SysUserInvitationDto(SysUserInvitationVo sysUserInvitationVo) {
        BeanUtils.copy(sysUserInvitationVo, this);
    }

    @JSONField(serialize = false)
    public SysUserInvitation getEntity() {
        return BeanUtils.copy(this, SysUserInvitation.class);
    }

    @JSONField(serialize = false)
    public SysUserInvitationVo getVo() {
        return BeanUtils.copy(this, SysUserInvitationVo.class);
    }

}