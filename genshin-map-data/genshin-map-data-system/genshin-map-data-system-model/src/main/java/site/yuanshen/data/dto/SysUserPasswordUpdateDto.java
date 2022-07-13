package site.yuanshen.data.dto;

import lombok.Data;

/**
 * TODO
 *
 * @author Moment
 */
@Data
public class SysUserPasswordUpdateDto {

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
