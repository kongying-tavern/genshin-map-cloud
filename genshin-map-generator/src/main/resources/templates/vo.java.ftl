package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

<#list table.importPackages as pkg>
    <#if !pkg?contains("mybatisplus") && !pkg?contains("BaseEntity")>
import ${pkg};
    </#if>
</#list>

/**
* ${table.comment}前端封装
*
* @since ${date}
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "${entity}前端封装", description = "${table.comment}前端封装")
public class ${entity}Vo {
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>

    <#if field.keyFlag>
        <#assign keyPropertyName = field.propertyName>
    </#if>
    <#if field.comment??>
    /**
     * ${field.comment}
     */
    @Schema(title = "${field.comment}")
    </#if>
    <#if field.propertyType == 'Timestamp'>
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

}