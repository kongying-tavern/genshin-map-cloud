package site.yuanshen.generator;

import cn.hutool.core.util.StrUtil;
import site.yuanshen.generator.utils.FastGenerator;

/**
 * MybatisPlus 代码生成器
 *
 * @author Moment
 *
 * 其中环境变量有以下设置途径：
 * 1. 通过系统环境变量进行设置，可能需要重启 IDEA
 * 2. 在启动 MBPGenerator#main 方法时，在配置中指定环境变量
 */
public class MBPGenerator {
    /**
     * url 需包含 schema 信息，且需确保用户能读到 pg_class，最好使用 postgres 账户。
     * 例子：<code>jdbc:postgresql://localhost:5432/genshin_map?currentSchema=genshin_map</code>
     */
    private static String url = System.getenv("GSAPI_DB_URL");

    private static String username = System.getenv("GSAPI_DB_USER");

    private static String password = System.getenv("GSAPI_DB_PASS");

    private static String author = System.getenv("GSAPI_AUTHOR");

    /**
     * 此处使用逗号分隔的表名，可以只对部分表进行生成，如果为空则生成所有实体。
     */
    private static String entity = System.getenv("GSAPI_ENTITY");

    public static void main(String[] args) throws Exception {
        FastGenerator generator = FastGenerator.getFastGenerator()
                .url(url)
                .userName(username)
                .password(password)
                .author(author)
                .entity(StrUtil.isBlank(entity) ? null : entity)
                .outputDir("/generator")
                .commentDateFormat("yyyy-MM-dd hh:mm:ss");
        generator.entityPackage("site.yuanshen.data.entity")
                .mapperPackage("site.yuanshen.data.mapper")
                .xmlPackage("mapper")
                .voPackage("site.yuanshen.data.vo")
                .dtoPackage("site.yuanshen.data.dto")
                .apiPackageName("site.yuanshen.genshin")
                .apiModuleName("core")
                .servicePackageAfterApi("service.mbp")
                .serviceImplPackageAfterApi("service.mbp.impl")
                .build();
    }
}
