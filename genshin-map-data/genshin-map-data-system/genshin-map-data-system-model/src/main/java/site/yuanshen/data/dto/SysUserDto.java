package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;

import java.sql.Timestamp;
import java.util.List;


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
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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

    /**
     * 权限策略
     */
    private List<String> accessPolicy;

    public SysUserDto(SysUser sysUser) {
        BeanUtils.copy(sysUser, this);
    }

    public SysUserDto(SysUserVo sysUserVo) {
        BeanUtils.copy(sysUserVo, this);
    }

    public SysUserDto(SysUserRegisterVo registerVo) {
        BeanUtils.copy(registerVo, this);
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
