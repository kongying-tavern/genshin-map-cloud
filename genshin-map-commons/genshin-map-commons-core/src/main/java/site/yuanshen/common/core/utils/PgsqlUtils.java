package site.yuanshen.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.*;
import java.util.stream.Collectors;

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
        private SFunction<E, ?> propGetter;
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
        private SFunction<E, ?> propGetter;
        // 比较器
        private Comparator<E> comparator;
        // 排序方式
        private Order order;
    }

    /**
     * 将排序列表转换为排序配置
     * @param sorts 排序列表
     * @param config 排序字段配置
     */
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

    /**
     * 对 QueryWrapper 使用排序
     * @param wrapper QueryWrapper 对象
     * @param sortList 排序配置列表
     */
    public static <E> QueryWrapper<E> sortWrapper(QueryWrapper<E> wrapper, List<Sort<E>> sortList) {
        for(Sort<E> sortItem : sortList) {
            final String sortField = sortItem.getField();
            wrapper = wrapper.orderBy(
                    StrUtil.isNotEmpty(sortField),
                    Order.ASC.equals(sortItem.getOrder()),
                    sortField
            );
        }
        return wrapper;
    }

    /**
     * 对 LambdaQueryWrapper 使用排序
     * @param wrapper LambdaQueryWrapper 对象
     * @param sortList 排序配置列表
     */
    public static <E> LambdaQueryWrapper<E> sortWrapper(LambdaQueryWrapper<E> wrapper, List<Sort<E>> sortList) {
        for(Sort<E> sortItem : sortList) {
            final SFunction<E, ?> sortPropGetter = sortItem.getPropGetter();
            wrapper = wrapper.orderBy(
                sortPropGetter != null,
                Order.ASC.equals(sortItem.getOrder()),
                sortPropGetter
            );
        }
        return wrapper;
    }

    /**
     * 对列表使用排序
     * @param list 列表
     * @param sortList 排序配置列表
     */
    public static <E> List<E> sortWrapper(List<E> list, List<Sort<E>> sortList) {
        if(CollUtil.isEmpty(sortList)) {
            return list;
        }

        // 构造组合式排序规则
        Comparator<E> combinedComparator = null;
        for(Sort<E> sortItem : sortList) {
            final Comparator<E> sortComparator = sortItem.getComparator();
            final Comparator<E> sortComparatorOrderd = Order.ASC.equals(sortItem.getOrder()) ? sortComparator : sortComparator.reversed();
            if(sortComparator == null) {
                continue;
            }
            if(combinedComparator == null) {
                combinedComparator = sortComparatorOrderd;
            } else {
                combinedComparator = combinedComparator.thenComparing(sortComparatorOrderd);
            }
        }

        // 应用排序规则
        if(combinedComparator == null) {
            return list;
        } else {
            return list.stream().sorted(combinedComparator).collect(Collectors.toList());
        }
    }

    /**
     * 获取分页后的列表
     * @param list 列表
     * @param current 当前页码
     * @param size 每页条数
     */
    public static <E> List<E> paginateWrapper(List<E> list, Long current, Long size) {
        if(current == null) {
            current = 1L;
        }
        if(size == null) {
            size = 10L;
        }

        long offset = current <= 1L ? 0L : Math.max((current - 1L) * size, 0L);
        int offsetNum = Math.toIntExact(offset);
        int sizeNum = Math.toIntExact(size);
        List<E> subList = CollUtil.sub(list, offsetNum, offsetNum + sizeNum);
        return subList;
    }
}
