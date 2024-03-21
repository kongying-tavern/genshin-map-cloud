package ${package.ServiceImpl};

import ${superServiceImplClassPackage};
import org.springframework.stereotype.Service;
import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};

/**
 * ${table.comment!} Mybatis Plus CRUD服务实现类
 *
 * @author ${author}
 */
@Service
public class ${table.serviceImplName} extends ${superServiceImplClass}<${table.mapperName}, ${entity}> implements ${table.serviceName} {

}
