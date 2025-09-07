package site.yuanshen.common.web.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserUtils {
    public static Long getUserId() {
        try {
            final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null)
                return null;
            final String userId = requestAttributes.getRequest().getHeader("userId");
            return Long.valueOf(userId);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserDataLevel() {
        try {
            final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null)
                return "";
            final String userDataLevel = requestAttributes.getRequest().getHeader("userDataLevel");
            return StrUtil.blankToDefault(userDataLevel, "");
        } catch (Exception e) {
            return "";
        }
    }
}
