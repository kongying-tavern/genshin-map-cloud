package site.yuanshen.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean工具类封装
 *
 * @author Moment
 */
public class BeanUtils {
    public static void copyNotNull(Object source, Object target) {
        BeanUtil.copyProperties(source,target, CopyOptions.create().ignoreNullValue().ignoreError());
    }



    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
    public static <T, R> T copyProperties(R r, Class<T> clazz) {
        T target = null;
        try {
            target = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("克隆异常", e);
        }
        return copyProperties(r, target);
    }

    public static <T, R> T copyProperties(R r, T t) {
        CachedBeanCopier.copyProperties(r,t);
        return t;
    }

}
