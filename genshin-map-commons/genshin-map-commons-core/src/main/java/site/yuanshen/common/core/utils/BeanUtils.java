package site.yuanshen.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

/**
 * Bean工具类封装
 *
 * @author Moment
 */
public class BeanUtils {

    public static void copyNotNull(Object source, Object target) {
        BeanUtil.copyProperties(source,target, CopyOptions.create().ignoreNullValue().ignoreError());
    }

}
