package site.yuanshen.common.core.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
        private List<String> ignore = new ArrayList<>();

        public FieldDiffConfig setIgnore(List<String> ignore) {
            this.ignore = ignore == null ? new ArrayList<>() : ignore;
            return this;
        }

        // 是否忽略更新前的 null
        private Boolean ignoreBeforeNull = true;

        public FieldDiffConfig setIgnoreBeforeNull(Boolean ignoreBeforeNull) {
            this.ignoreBeforeNull = ignoreBeforeNull == null || ignoreBeforeNull;
            return this;
        }

        // 是否忽略更新后的 null
        private Boolean ignoreAfterNull = true;

        public FieldDiffConfig setIgnoreAfterNull(Boolean ignoreAfterNull) {
            this.ignoreAfterNull = ignoreAfterNull == null || ignoreAfterNull;
            return this;
        }

        // 自定义比较器，默认比较器为 Objects.deepEqual
        private Map<String, BiPredicate<Object, Object>> comparators = new HashMap<>();

        public FieldDiffConfig setComparators(String key, BiPredicate<Object, Object> comparator) {
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
        if(CollectionUtil.isEmpty(cfDiffFields)) {
            List<Field> dtBeforeFields = ClassUtils.getFields(before.getClass());
            List<String> dtBeforeFieldNames = dtBeforeFields.stream().filter(Objects::nonNull).map(Field::getName).collect(Collectors.toList());
            List<Field> dtAfterFields = ClassUtils.getFields(after.getClass());
            List<String> dtAfterFieldNames = dtAfterFields.stream().filter(Objects::nonNull).map(Field::getName).collect(Collectors.toList());
            cfDiffFields = CollectionUtil.union(dtBeforeFieldNames, dtAfterFieldNames).stream().filter(v -> v != null).distinct().collect(Collectors.toList());
        }

        // 2. 删除忽略字段
        List<String> cfIgnoreFields = config.getIgnore();
        if(CollectionUtil.isNotEmpty(cfIgnoreFields)) {
            cfDiffFields = CollectionUtil.subtract(cfDiffFields, cfIgnoreFields).stream().collect(Collectors.toList());
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
            boolean cpIsEqual;
            Map<String, BiPredicate<Object, Object>> cfComparators = config.getComparators();
            BiPredicate<Object, Object> cfComparator = cfComparators.get(cfDiffField);
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

    @Data
    @Accessors(chain = true )
    public static class Levenshtein {
        public Levenshtein(String s1, String s2) {
            this.setText(s1, s2);
        }

        // 差异距离
        private Integer distance;
        // 相似度
        private BigDecimal similarity;
        // 文本
        private String text1;

        public Levenshtein setText1(String text1) {
            this.text1 = StrUtil.emptyToDefault(text1, "");
            return this;
        }

        private String text2;

        public Levenshtein setText2(String text2) {
            this.text2 = StrUtil.emptyToDefault(text2, "");
            return this;
        }

        public Levenshtein setText(String s1, String s2) {
            this.setText1(s1);
            this.setText2(s2);
            return this;
        }

        public Levenshtein calculateDistance() {
            int distance[][];
            final String s1 = this.text1;
            final int s1Len = s1.length();
            final String s2 = this.text2;
            final int s2Len = s2.length();

            if(s1Len == 0) {
                this.distance = s2Len;
                return this;
            }
            if(s2Len == 0) {
                this.distance = s1Len;
                return this;
            }
            distance = new int[s1Len + 1][s2Len + 1];

            // 二维数组初始化
            for(int i = 0; i <= s1Len; i++) {
                distance[i][0] = i;
            }
            for(int j = 0; j <= s2Len; j++) {
                distance[0][j] = j;
            }

            for(int i = 1; i <= s1Len; i++) {
                final String s1Char = s1.substring(i - 1, i);
                for(int j = 1; j <= s2Len; j++) {
                    final String s2Char = s2.substring(j - 1, j);

                    if(s1Char.equals(s2Char)) {
                        // 若相等，则代价为0，取左上方值
                        distance[i][j] = distance[i - 1][j - 1];
                    } else {
                        // 若不等，则代价为1，取左、上、左上角最小值 + 代价
                        distance[i][j] = Math.min(Math.min(distance[i - 1][j], distance[i][j - 1]), distance[i - 1][j - 1]) + 1;
                    }
                }
            }
            this.distance = distance[s1Len][s2Len];

            return this;
        }

        public Levenshtein calculateSimilarity() {
            if(this.distance == null) {
                this.calculateDistance();
            }
            final int s1Len = this.text1.length();
            final int s2Len = this.text2.length();
            final BigDecimal similarity = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(distance).divide(BigDecimal.valueOf(Math.max(s1Len, s2Len)))
            );
            this.similarity = similarity;

            return this;
        }
    }
}
