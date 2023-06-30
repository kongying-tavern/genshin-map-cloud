package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "SysUser前端精简封装", description = "系统用户表前端精简封装")
public class SysUserSmallVo {
    @Schema(title = "用户名")
    private String username;

    @Schema(title = "昵称")
    private String nickname;

    @Schema(title = "QQ")
    private String qq;

    @Schema(title = "手机号")
    private String phone;

    @Schema(title = "头像链接")
    private String logo;
}
