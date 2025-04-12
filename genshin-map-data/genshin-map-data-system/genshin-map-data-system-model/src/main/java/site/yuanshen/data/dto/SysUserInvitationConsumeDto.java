package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.SysUserInvitationConsumeVo;

import java.util.List;


/**
 * 用户邀请消费数据封装
 *
 * @since 2025-03-26 07:46:02
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户邀请消费数据封装", description = "用户邀请消费邀请数据封装")
public class SysUserInvitationConsumeDto {

    /**
     * 邀请码
     */
    private String code;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    public SysUserInvitationConsumeDto(SysUserInvitationConsumeVo sysUserInvitationConsumeVo) {
        BeanUtils.copy(sysUserInvitationConsumeVo, this);
    }

    @JSONField(serialize = false)
    public SysUserInvitationConsumeVo getVo() {
        return BeanUtils.copy(this, SysUserInvitationConsumeVo.class);
    }

}