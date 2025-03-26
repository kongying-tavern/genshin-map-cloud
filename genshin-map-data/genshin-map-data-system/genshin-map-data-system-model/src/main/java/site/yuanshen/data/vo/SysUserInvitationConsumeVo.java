package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户邀请消费数据封装", description = "用户邀请消费数据封装")
public class SysUserInvitationConsumeVo {

    /**
     * 邀请码
     */
    @Schema(title = "邀请码")
    private String code;

    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(title = "密码")
    private String password;

    /**
     * 昵称
     */
    @Schema(title = "昵称")
    private String nickname;
}
