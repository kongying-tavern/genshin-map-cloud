package site.yuanshen.common.core.utils;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * 为了应付pg里的奇妙查询格式
 * @author Sunosay
 */
public class PgsqlUtils {
    public static String unnestStr(List<Long> markerIdList) {
        String s = JSON.toJSONString(markerIdList);
        return "'{"+s.substring(1,s.length()-1)+"}'";
    }
}
