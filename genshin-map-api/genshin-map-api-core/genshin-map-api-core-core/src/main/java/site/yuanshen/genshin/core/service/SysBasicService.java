package site.yuanshen.genshin.core.service;

import site.yuanshen.data.entity.SysUser;

import java.util.Optional;

/**
 * 部分内部用的公共用户、角色服务（规避循环依赖）
 *
 * @author Moment
 */
public interface SysBasicService {

    /**
     * 此方法建议只用于同级service
     *
     * @param Id 用户ID
     * @return 用户实体类Optional
     */
    Optional<SysUser> getUser(Long Id);

    /**
     * 此方法建议只用于同级service
     *
     * @param id 用户ID
     * @return 用户实体类
     */
    SysUser getUserNotNull(Long id);

    /**
     * 此方法建议只用于同级service
     *
     * @param userName 用户名
     * @return 用户实体类Optional
     */
    Optional<SysUser> getUser(String userName);

}