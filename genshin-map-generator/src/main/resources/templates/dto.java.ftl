package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.${entity};
import site.yuanshen.data.vo.${entity}Vo;
<#list table.importPackages as pkg>
    <#if !pkg?contains("mybatisplus") && !pkg?contains("BaseEntity")>
import ${pkg};
    </#if>
</#list>


/**
 * ${table.comment}数据封装
 *
 * @since ${date}
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "${entity}数据封装", description = "${table.comment}数据封装")
public class ${entity}Dto {
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>

<#if field.keyFlag>
    <#assign keyPropertyName = field.propertyName>
</#if>
<#if field.comment??>
    /**
     * ${field.comment}
     */
</#if>
<#if field.propertyType == 'Timestamp'>
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
</#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

    public ${entity}Dto(${entity} ${entity?uncap_first}) {
        BeanUtils.copy(${entity?uncap_first}, this);
    }

    public ${entity}Dto(${entity}Vo ${entity?uncap_first}Vo) {
        BeanUtils.copy(${entity?uncap_first}Vo, this);
    }

    @JSONField(serialize = false)
    public ${entity} getEntity() {
        return BeanUtils.copy(this, ${entity}.class);
    }

    @JSONField(serialize = false)
    public ${entity}Vo getVo() {
        return BeanUtils.copy(this, ${entity}Vo.class);
    }

}