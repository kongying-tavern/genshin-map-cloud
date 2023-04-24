package site.yuanshen.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.extra.cglib.CglibUtil;
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

    public static <T, R> T copy(R r, Class<T> clazz) {
        return CglibUtil.copy(r, clazz);
    }

    public static <T, R> T copy(R r, T t) {
        CglibUtil.copy(r,t);
        return t;
    }

}
