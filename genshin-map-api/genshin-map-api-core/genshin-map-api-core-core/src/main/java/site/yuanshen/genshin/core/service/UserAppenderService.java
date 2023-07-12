package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.utils.ApplicationUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.SysUserVo;

import java.util.*;
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

        final Map<Long, SysUserSmallVo> userVoMap = getUserMap(list, getterSrc);


        for(T item : list) {
            Long userId = 0L;
            try {
                userId = getterTarget.apply(item);
            } catch (Exception e) {
                // nothing to do
            }
            final SysUserSmallVo user = userVoMap.getOrDefault(userId, new SysUserSmallVo());
            try {
                setterTarget.accept(item, user);
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

    public static <D, T> R appendUser(
            R resultData,
            D data,
            boolean isList,
            Function<T, Long> idGetter
    ) {
        List<T> list = new ArrayList<>();
        try {
            if(isList) {
                list = (List<T>) data;
            } else {
                list = Arrays.asList((T) data);
            }
        } catch (Exception e) {
            // nothing to do
        }

        final Map<Long, SysUserSmallVo> newUserMap = getUserMap(list, idGetter);
        final Map<Long, SysUserSmallVo> oldUserMap = resultData.getUsers() == null ? new HashMap<>() : resultData.getUsers();
        oldUserMap.putAll(newUserMap);
        resultData.setUsers(oldUserMap);

        return resultData;
    }

    /**
     * 获取用户数据映射
     * @param list      列表
     * @param getterSrc 从原列表获取用户ID的 get 方法
     */
    public static <T> Map<Long, SysUserSmallVo> getUserMap(List<T> list, Function<T, Long> getterSrc) {
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

        if(CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }

        final SysUserMapper sysUserMapper = ApplicationUtils.getBean(SysUserMapper.class);

        final List<SysUser> userList = sysUserMapper.selectUserWithDelete(userIds);
        final Map<Long, SysUserSmallVo> userVoMap = userList.stream()
                .filter(Objects::nonNull)
                .map(o -> new SysUserDto(o).getVo())
                .collect(Collectors.toMap(
                        SysUserVo::getId,
                        v -> BeanUtils.copy(v, SysUserSmallVo.class),
                        (o, n) -> n
                ));

        return userVoMap;
    }
}
