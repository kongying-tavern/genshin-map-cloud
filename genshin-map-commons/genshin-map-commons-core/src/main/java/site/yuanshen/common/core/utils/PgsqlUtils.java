package site.yuanshen.common.core.utils;

import com.alibaba.fastjson2.JSON;

import java.util.Collections;
import java.util.List;

/**
 * 为了应付pg里的奇妙查询格式
 * @author Sunosay
 */
public class PgsqlUtils {
    public static String unnestLongStr(List<Long> longList) {
        String s = JSON.toJSONString(longList);
        return "'{" + s.substring(1,s.length()-1) + "}'";
    }

    public static String unnestStringStr(List<String> strList) {
        String s = JSON.toJSONString(strList);
        s = s.replace("\"", "");
        return "'{" + s.substring(1,s.length()-1) + "}'";
    }
}
