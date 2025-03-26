package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
* 系统用户邀请表;系统用户邀请前端封装
*
* @since 2025-03-26 07:46:02
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserInvitation前端封装", description = "系统用户邀请表;系统用户邀请前端封装")
public class SysUserInvitationSmallVo {

    @Schema(title = "邀请码")
    private String code;

    @Schema(title = "用户名")
    private String username;

}