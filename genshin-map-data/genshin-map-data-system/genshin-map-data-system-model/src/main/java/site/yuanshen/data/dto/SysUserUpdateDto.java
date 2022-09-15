package site.yuanshen.data.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * TODO
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
