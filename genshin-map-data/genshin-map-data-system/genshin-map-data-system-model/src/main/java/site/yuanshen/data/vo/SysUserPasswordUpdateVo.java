package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * 用户密码修改前端封装
 *
 * @author Moment
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户密码修改前端封装", description = "用户密码修改前端封装, 管理员使用时可以将旧密码留空")
public class SysUserPasswordUpdateVo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 密码
     */
    private String password;

    /**
     * 旧密码
     */
    private String oldPassword;

}
