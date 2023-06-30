package site.yuanshen.genshin.core.service;

import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.web.utils.ApplicationUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.SysUserVo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserAppenderService {
    /**
     * 向列表附加用户数据
     * @param list         列表
     * @param getterSrc    从原列表获取用户ID的 get 方法
     * @param getterTarget 从目标列表获取用户ID的 get 方法
     * @param setterTarget 向目标列表放置用户数据的 set 方法
     */
    public static <T> void appendUser(
            List<T> list,
            Function<T, Long> getterSrc,
            Function<T, Long> getterTarget,
            BiConsumer<T, SysUserSmallVo> setterTarget
    ) {
        final List<Long> userIds = list.stream()
                .filter(Objects::nonNull)
                .map(v -> {
                    try {
                        return getterSrc.apply(v);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        final SysUserMapper sysUserMapper = ApplicationUtils.getBean(SysUserMapper.class);

        final List<SysUser> userList = sysUserMapper.selectUserWithDelete(userIds);
        final Map<Long, SysUserVo> userVos = userList.stream()
                .filter(Objects::nonNull)
                .map(o -> (new SysUserDto(o)).getVo())
                .collect(Collectors.toMap(
                        SysUserVo::getId,
                        v -> v,
                        (o, n) -> n
                ));

        for(T item : list) {
            Long userId = 0L;
            try {
                userId = getterTarget.apply(item);
            } catch (Exception e) {
                // nothing to do
            }
            final SysUserVo user = userVos.getOrDefault(userId, new SysUserVo());
            final SysUserSmallVo userVo = BeanUtils.copy(user, SysUserSmallVo.class);
            try {
                setterTarget.accept(item, userVo);
            } catch (Exception e) {
                // nothing to do
            }
        }
    }

    /**
     * 向对象附加用户数据
     * @param clazz        对象对应的类
     * @param item         对象数据
     * @param getterSrc    从原列表获取用户ID的 get 方法
     * @param getterTarget 从目标列表获取用户ID的 get 方法
     * @param setterTarget 向目标列表放置用户数据的 set 方法
     */
    public static <T> T appendUser(
            Class<T> clazz,
            T item,
            Function<T, Long> getterSrc,
            Function<T, Long> getterTarget,
            BiConsumer<T, SysUserSmallVo> setterTarget
    ) {
        List<T> list = Arrays.asList(item);
        appendUser(list, getterSrc, getterTarget, setterTarget);
        if(list.size() > 0) {
            return list.get(0);
        } else {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
