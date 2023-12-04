package site.yuanshen.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    public static String unnestStringStr(List<String> strList) {
        String s = JSON.toJSONString(strList);
        s = s.replace("\"", "");
        return "'{" + s.substring(1,s.length()-1) + "}'";
    }

    public enum Order {
        ASC,
        DESC
    }

    @NoArgsConstructor
    @Data
    public static class Sort<E> {
        private String field = "";
        private String prop = "";
        private Function<E, ?> osGetter;
        private Order order;
    }

    public static <E> List<Sort<E>> toSort(List<String> sorts, Class<E> clazz, Set<String> allowProps) {
        if (CollUtil.isEmpty(sorts)) {
            return List.of();
        }

        // 提取字段名和初始排序映射
        final Map<String, Order> sortOrderMap = new HashMap<>();
        final List<String> sortPropList = new ArrayList<>();
        final Set<String> sortPropSet = new HashSet<>();
        for (String sortChunk : sorts) {
            if(StrUtil.isBlank(sortChunk)) {
                continue;
            }

            String sortProp = sortChunk.substring(0, sortChunk.length() - 1);
            String sortOrder = sortChunk.substring(sortChunk.length() - 1);
            if (
                    StrUtil.equalsAnyIgnoreCase(sortOrder, "+", "-") &&
                    StrUtil.isNotBlank(sortProp)
            ) {
                if (CollUtil.isEmpty(allowProps) || !allowProps.contains(sortProp)) {
                    continue;
                }

                final Order sortOrderEnum = "+".equals(sortOrder) ? Order.ASC : Order.DESC;
                sortPropSet.add(sortProp);
                sortPropList.add(sortProp);
                sortOrderMap.put(sortProp, sortOrderEnum);
            }
        }

        // 提取字段映射
        final List<Field> fields = ClassUtils.getFields(clazz);
        final Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            final String propName = field.getName();
            if (sortPropSet.contains(propName)) {
                fieldMap.put(propName, field);
            }
        }

        // 构造排序
        final List<Sort<E>> sortList = new ArrayList<>();
        for (String propName : sortPropList) {
            if (!sortPropSet.contains(propName)) {
                continue;
            }

            final Order sortOrder = sortOrderMap.get(propName);
            final Field sortField = fieldMap.get(propName);
            if (sortOrder == null || sortField == null) {
                continue;
            }

            String sortFieldName = "";
            final TableField annotation = sortField.getDeclaredAnnotation(TableField.class);
            if(annotation != null) {
                sortFieldName = StrUtil.blankToDefault(annotation.value(), "");
            }
            if(StrUtil.isBlank(sortFieldName)) {
                sortFieldName = NamingCase.toSymbolCase(propName, '_');
            }

            Function<E, ?> sortGetterFunction = null;
            try {
                final String sortGetterName = "get" + NamingCase.toPascalCase(propName);
                final Method sortGetter = clazz.getDeclaredMethod(sortGetterName, new Class[]{});
                sortGetterFunction = (i) -> {
                    try {
                        return sortGetter.invoke(i);
                    } catch(Exception ex) {
                        return null;
                    }
                };
            } catch (Exception e) {
                // nothing
            }

            Sort<E> sort = (new Sort<>());
            sort.setField(sortFieldName);
            sort.setProp(propName);
            sort.setOsGetter(sortGetterFunction);
            sort.setOrder(sortOrder);
            sortList.add(sort);
        }

        return sortList;
    }

    public static <E> QueryWrapper<E> sortWrapper(QueryWrapper<E> wrapper, List<Sort<E>> sortList) {
        for (Sort<E> sortItem : sortList) {
            wrapper = wrapper.orderBy(
                    StrUtil.isNotEmpty(sortItem.getField()),
                    Order.ASC.equals(sortItem.getOrder()),
                    sortItem.getField()
            );
        }
        return wrapper;
    }
}
