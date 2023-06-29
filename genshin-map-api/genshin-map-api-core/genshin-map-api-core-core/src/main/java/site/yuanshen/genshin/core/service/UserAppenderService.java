package site.yuanshen.genshin.core.service;

import site.yuanshen.common.web.utils.ApplicationUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserVo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserAppenderService {
    public static <T> void appendUser(
            List<T> list,
            Function<T, Long> getterSrc,
            Function<T, Long> getterTarget,
            BiConsumer<T, SysUserVo> setterTarget
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
            try {
                setterTarget.accept(item, user);
            } catch (Exception e) {
                // nothing to do
            }
        }
    }
}
