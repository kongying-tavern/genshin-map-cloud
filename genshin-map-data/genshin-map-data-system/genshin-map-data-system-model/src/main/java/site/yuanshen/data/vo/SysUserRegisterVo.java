package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户注册Vo
 *
 * @author Moment
 */
@Data
@Schema(title = "用户注册Vo", description = "用户注册Vo")
public class SysUserRegisterVo {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
