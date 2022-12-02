package site.yuanshen.data.dto;

import lombok.Data;

/**
 * 用户密码修改Dto
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
