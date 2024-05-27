package site.yuanshen.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.*;
import java.util.function.Function;

/**
 * 为了应付pg里的奇妙查询格式
 * @author Sunosay
 */
public class PgsqlUtils {
    public static String unnestLongStr(List<Long> longList) {
        String s = JSON.toJSONString(longList);
        return "'{" + s.substring(1,s.length()-1) + "}'";
    }

    public static String unnestLongStr(Set<Long> longSet) {
        return unnestLongStr(new ArrayList<>(longSet));
    }

    public static String unnestStringStr(List<String> strList) {
        String s = JSON.toJSONString(strList);
        s = s.replace("\"", "");
        return "'{" + s.substring(1,s.length()-1) + "}'";
    }

    public static String unnestStringStr(Set<String> strSet) {
        return unnestStringStr(new ArrayList<>(strSet));
    }

    /**
     * ------------------------------
     * 排序相关
     * ------------------------------
     */

    /**
     * 排序顺序
     */
    public enum Order {
        ASC,
        DESC
    }

    /**
     * 排序配置
     */
    @NoArgsConstructor
    @Data
    public static class SortConfig<E> {
        public static <E> SortConfig<E> create() {
            return new SortConfig<>();
        }

        private Map<String, SortConfigItem<E>> entries = new HashMap<>();

        public SortConfig<E> addEntry(SortConfigItem<E> entry) {
            if(entry == null) {
                return this;
            }

            final String propertyName = entry.getProp();
            if(StrUtil.isBlank(propertyName)) {
                return this;
            }
            entries.put(propertyName, entry);

            return this;
        }
    }

    /**
     * 排序配置项
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @With
    public static class SortConfigItem<E> {
        public static <E> SortConfigItem<E> create() {
            return new SortConfigItem();
        }

        // 数据库字段
        private String prop = "";
        // 代码字段
        private Function<E, ?> propGetter;
        // 比较器
        private Comparator<E> comparator;
    }

    /**
     * 排序配置
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @With
    public static class Sort<E> {
        public static <E> Sort<E> create() {
            return new Sort<>();
        }

        // 数据库字段
        private String field = "";
        // 代码字段
        private String prop = "";
        // 代码字段获取
        private Function<E, ?> propGetter;
        // 比较器
        private Comparator<E> comparator;
        // 排序方式
        private Order order;
    }

    public static <E> List<Sort<E>> toSortConfigurations(List<String> sorts, SortConfig<E> config) {
        if (CollUtil.isEmpty(sorts)) {
            return List.of();
        }

        // 提取字段名和初始排序映射
        List<Sort<E>> sortList = new ArrayList<>();
        for(String sortString : sorts) {
            if(StrUtil.isBlank(sortString)) {
                continue;
            }

            int sortStringLen = sortString.length();
            final String sortProperty = sortString.substring(0, sortStringLen - 1);
            final String sortOrder = sortString.substring(sortStringLen - 1);
            if(StrUtil.isBlank(sortProperty)) {
                continue;
            } else if(!StrUtil.equalsAny(sortOrder, "+", "-")) {
                continue;
            }
            final String sortField = NamingCase.toSymbolCase(sortProperty, '_');
            final Order sortOrderEnum = StrUtil.equals(sortOrder, "+") ? Order.ASC : Order.DESC;
            // 提取配置中的数据
            final SortConfigItem<E> sortConfig = config.getEntries().get(sortProperty);
            if(sortConfig == null) {
                continue;
            }

            // 构造排序
            sortList.add(Sort.<E>create()
                    .withField(sortField)
                    .withProp(sortProperty)
                    .withPropGetter(sortConfig.getPropGetter())
                    .withComparator(sortConfig.getComparator())
                    .withOrder(sortOrderEnum)
            );
        }

        return sortList;
    }

    public static <E> QueryWrapper<E> sortWrapper(QueryWrapper<E> wrapper, List<Sort<E>> sortList) {
        for (Sort<E> sortItem : sortList) {
            final String sortField = sortItem.getField();
            wrapper = wrapper.orderBy(
                    StrUtil.isNotEmpty(sortField),
                    Order.ASC.equals(sortItem.getOrder()),
                    sortField
            );
        }
        return wrapper;
    }
}
