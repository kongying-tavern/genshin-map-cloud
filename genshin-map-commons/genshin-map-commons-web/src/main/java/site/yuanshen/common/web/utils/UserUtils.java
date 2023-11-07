package site.yuanshen.common.web.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserUtils {
    public static Long getUserId() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) return null;
            String userId = requestAttributes.getRequest().getHeader("userId");
            return Long.valueOf(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
