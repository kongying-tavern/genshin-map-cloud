package site.yuanshen.common.core.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityUtils {
    /**
     * 列处理选项
     */
    @Getter
    public static class ColumnPreference {
        public static ColumnPreference create() {
            return new ColumnPreference();
        }

        private List<String> columnsAllow;

        public ColumnPreference setColumnsAllow(List<String> columnsAllow) {
            this.columnsAllow = columnsAllow;
            return this;
        }
    }

    /**
     * 字段配置
     */
    @Data
    public static class ColumnConfig {
        // 数据库字段名
        private String dbFieldName;

        // 属性名
        private String propertyName;
    }

    /**
     * 字段配置数据封装
     */
    @Data
    public static class ColumnConfigPack {
        // 主体字段的配置
        private List<ColumnConfig> main = new ArrayList<>();

        // ID 的字段配置
        private ColumnConfig id;
    }

    /**
     * 获取实体列属性
     * @param clazz 实体类
     * @param pref 字段生成配置
     * @return 实体列属性配置集合
     */
    public static ColumnConfigPack getEntityColumns(Class clazz, ColumnPreference pref) {
        ColumnConfigPack columns = new ColumnConfigPack();
        ColumnConfig columnsId = null;
        List<ColumnConfig> columnsMain = new ArrayList();
        List<String> columnsAllow = pref.getColumnsAllow();

        List<Field> fields = ClassUtils.getFields(clazz);
        for(Field field : fields) {
            final TableField fieldAnnotation = field.getDeclaredAnnotation(TableField.class);
            final TableId fieldIdAnnotation = field.getDeclaredAnnotation(TableId.class);
            final String fieldDbName = fieldAnnotation == null ? "" : fieldAnnotation.value();
            final String fieldName = field.getName();

            if(StrUtil.isNotBlank(fieldDbName) && StrUtil.isNotBlank(fieldName)) {
                final ColumnConfig fieldConfig = new ColumnConfig();
                fieldConfig.setDbFieldName(fieldDbName);
                fieldConfig.setPropertyName(fieldName);

                if(fieldIdAnnotation == null) {
                    if(columnsAllow != null && !columnsAllow.contains(fieldName)) {
                        continue;
                    }
                    columnsMain.add(fieldConfig);
                } else {
                    columnsId = fieldConfig;
                }
            }
        }

        columns.setId(columnsId);
        columns.setMain(columnsMain);

        return columns;
    }

    /**
     * 获取表名
     * @param clazz 实体类
     * @return 表名
     */
    public static String getEntityTableName(Class clazz) {
        String tableName = "";
        try {
            final TableName tableAnnotation = (TableName) clazz.getDeclaredAnnotation(TableName.class);
            tableName = tableAnnotation == null ? "" : tableAnnotation.value();
        } catch (Exception e) {
            // nothing to do
        }
        return tableName;
    }
}
