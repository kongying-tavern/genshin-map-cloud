package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 系统用户表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUser前端封装", description = "系统用户表前端封装")
public class SysUserVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(title = "昵称")
    private String nickname;

    /**
     * QQ
     */
    @Schema(title = "QQ")
    private String qq;

    /**
     * 手机号
     */
    @Schema(title = "手机号")
    private String phone;

    /**
     * 头像链接
     */
    @Schema(title = "头像链接")
    private String logo;

    /**
     * 角色ID
     */
    @Schema(title = "角色ID")
    private Integer roleId;

}