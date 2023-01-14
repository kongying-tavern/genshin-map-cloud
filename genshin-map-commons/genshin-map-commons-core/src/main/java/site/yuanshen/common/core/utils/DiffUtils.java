package site.yuanshen.common.core.utils;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiffUtils {
    @Data
    public static class FieldDiff {
        private String key = "";
        // 更新前数据
        private Object before;
        // 更新后的数据
        private Object after;
    }

    @Getter
    public static class FieldDiffConfig {
        public static FieldDiffConfig create() {
            return new FieldDiffConfig();
        }

        // 需要判断的字段，为空则表示全部
        private List<String> fields = new ArrayList<>();

        public FieldDiffConfig setFields(List<String> fields) {
            this.fields = fields == null ? new ArrayList<>() : fields;
            return this;
        }

        // 需要忽略的字段
        private List<String> ignore = new ArrayList();

        public FieldDiffConfig setIgnore(List<String> ignore) {
            this.ignore = ignore == null ? new ArrayList<>() : ignore;
            return this;
        }

        // 是否忽略更新前的 null
        private Boolean ignoreBeforeNull = true;

        public FieldDiffConfig setIgnoreBeforeNull(Boolean ignoreBeforeNull) {
            this.ignoreBeforeNull = ignoreBeforeNull == null ? true : ignoreBeforeNull;
            return this;
        }

        // 是否忽略更新后的 null
        private Boolean ignoreAfterNull = true;

        public FieldDiffConfig setIgnoreAfterNull(Boolean ignoreAfterNull) {
            this.ignoreAfterNull = ignoreAfterNull == null ? true : ignoreAfterNull;
            return this;
        }

        // 自定义比较器，默认比较器为 Objects.deepEqual
        private Map<String, BiPredicate> comparators = new HashMap<>();

        public FieldDiffConfig setComparators(String key, BiPredicate comparator) {
            if(this.comparators == null) {
                this.comparators = new HashMap<>();
            }
            this.comparators.put(key, comparator);
            return this;
        }

        // 比对之前对数据的处理
        private Map<String, Function> actionsPre = new HashMap<>();

        public FieldDiffConfig setActionsPre(String key, Function action) {
            if(this.actionsPre == null) {
                this.actionsPre = new HashMap<>();
            }
            this.actionsPre.put(key, action);
            return this;
        }

        // 比对之后对数据的处理
        private Map<String, Function> actionsPost = new HashMap<>();

        public FieldDiffConfig setActionsPost(String key, Function action) {
            if(this.actionsPost == null) {
                this.actionsPost = new HashMap<>();
            }
            this.actionsPost.put(key, action);
            return this;
        }
    }

    /**
     * 获取字段差异
     * @param before 变更之前的数据
     * @param after 变更之后的数据
     * @param config 字段比对配置
     * @return 有差异的字段列表
     */
    public static List<FieldDiff> getFieldsDiff(Object before, Object after, FieldDiffConfig config) {
        List<FieldDiff> fieldsDiff = new ArrayList<>();

        // 1. 数据字段结构构造
        List<String> cfDiffFields = config.getFields();
        if(CollectionUtils.isEmpty(cfDiffFields)) {
            List<Field> dtBeforeFields = ClassUtils.getFields(before.getClass());
            List<String> dtBeforeFieldNames = dtBeforeFields.stream().filter(v -> v != null).map(v -> v.getName()).collect(Collectors.toList());
            List<Field> dtAfterFields = ClassUtils.getFields(after.getClass());
            List<String> dtAfterFieldNames = dtAfterFields.stream().filter(v -> v != null).map(v -> v.getName()).collect(Collectors.toList());
            cfDiffFields = CollectionUtils.union(dtBeforeFieldNames, dtAfterFieldNames).stream().filter(v -> v != null).distinct().collect(Collectors.toList());
        }

        // 2. 删除忽略字段
        List<String> cfIgnoreFields = config.getIgnore();
        if(CollectionUtils.isNotEmpty(cfIgnoreFields)) {
            cfDiffFields = CollectionUtils.subtract(cfDiffFields, cfDiffFields).stream().collect(Collectors.toList());
        }

        // 3. 比较字段值
        final Map<String, Function> cfActionsPre = config.getActionsPre();
        final Map<String, Function> cfActionsPost = config.getActionsPost();
        final Boolean cfIgnoreNullForBefore = config.getIgnoreBeforeNull();
        final Boolean cfIgnoreNullForAfter = config.getIgnoreAfterNull();
        for(String cfDiffField : cfDiffFields) {
            Object dtBeforeVal = ClassUtils.getValue(before, cfDiffField);
            Object dtAfterVal = ClassUtils.getValue(after, cfDiffField);

            // 3.1 预处理数据
            Function cfActionPre = cfActionsPre.get(cfDiffField);
            if(cfActionPre != null) {
                dtBeforeVal = cfActionPre.apply(dtBeforeVal);
                dtAfterVal = cfActionPre.apply(dtAfterVal);
            }

            // 3.2 比对数据
            // 3.2.1 自定义比较逻辑
            boolean cpIsEqual = false;
            Map<String, BiPredicate> cfComparators = config.getComparators();
            BiPredicate cfComparator = cfComparators.get(cfDiffField);
            if(cfComparator != null) {
                cpIsEqual = cfComparator.test(dtBeforeVal, dtAfterVal);
            } else {
                cpIsEqual = Objects.deepEquals(dtBeforeVal, dtAfterVal);
            }

            if(!cpIsEqual) {
                // 3.2.1 处理空值忽略逻辑
                if(dtBeforeVal == null && cfIgnoreNullForBefore) {
                    continue;
                }
                if(dtAfterVal == null && cfIgnoreNullForAfter) {
                    continue;
                }

                // 3.2.2 后处理数据
                Function cfActionPost = cfActionsPost.get(cfDiffField);
                if(cfActionPost != null) {
                    dtBeforeVal = cfActionPost.apply(dtBeforeVal);
                    dtAfterVal = cfActionPost.apply(dtAfterVal);
                }

                // 3.2.3 添加差异条目
                FieldDiff fieldDiff = new FieldDiff();
                fieldDiff.setKey(cfDiffField);
                fieldDiff.setBefore(dtBeforeVal);
                fieldDiff.setAfter(dtAfterVal);
                fieldsDiff.add(fieldDiff);
            }
        }

        return fieldsDiff;
    }
}
