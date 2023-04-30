package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.vo.SysUserVo;
import java.time.LocalDateTime;


/**
 * 系统用户数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUser数据封装", description = "系统用户表数据封装")
public class SysUserDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户名
     */
    private String username;

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
    private String logo;

    /**
     * 角色ID
     */
    private Integer roleId;

    public SysUserDto(SysUser sysUser) {
        BeanUtils.copy(sysUser, this);
    }

    public SysUserDto(SysUserVo sysUserVo) {
        BeanUtils.copy(sysUserVo, this);
    }

    @JSONField(serialize = false)
    public SysUser getEntity() {
        return BeanUtils.copy(this, SysUser.class);
    }

    @JSONField(serialize = false)
    public SysUserVo getVo() {
        return BeanUtils.copy(this, SysUserVo.class);
    }

}