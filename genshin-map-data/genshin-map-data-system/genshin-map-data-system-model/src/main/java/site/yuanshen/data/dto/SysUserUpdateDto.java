package site.yuanshen.data.dto;

import lombok.Data;

/**
 * 用户信息更新Dto
 *
 * @author Moment
 */
@Data
public class SysUserUpdateDto {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * QQ
     */
    private String qq;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像链接
     */
    private String logoUrl;

}
