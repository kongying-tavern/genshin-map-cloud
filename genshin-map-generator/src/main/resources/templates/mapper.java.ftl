package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
<#if mapperAnnotation>
import org.apache.ibatis.annotations.Mapper;
</#if>

/**
 * ${table.comment!} Mapper 接口
 *
 * @since ${date}
 */
<#if mapperAnnotation>
@Mapper
</#if>
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

}
