package site.yuanshen.generator;

import site.yuanshen.generator.utils.FastGenerator;

/**
 * MybatisPlus 代码生成器
 *
 * @author Moment
 */
public class MBPGenerator {
    /**
     * url，需包含schema信息，且需确保用户能读到pg_class，最好使用postgres账户，例子：jdbc:postgresql://localhost:5432/genshin_map?currentSchema=genshin_map
     */
    private static String url = System.getenv("GSAPI_DB_URL");

    private static String username = System.getenv("GSAPI_DB_USER");

    private static String password = System.getenv("GSAPI_DB_PASS");

    private static String author = System.getenv("GSAPI_AUTHOR");

    public static void main(String[] args) throws Exception {
        FastGenerator generator = FastGenerator.getFastGenerator()
                .url(url)
                .userName(username)
                .password(password)
                .author(author)
                .outputDir("/generator")
                .commentDateFormat("yyyy-MM-dd hh:mm:ss");
        generator.entityPackage("site.yuanshen.data.entity")
                .mapperPackage("site.yuanshen.data.mapper")
                .xmlPackage("mapper")
                .voPackage("site.yuanshen.data.vo")
                .dtoPackage("site.yuanshen.data.dto")
                .apiPackageName("site.yuanshen.api")
                .apiModuleName("core")
                .servicePackageAfterApi("service.mbp")
                .serviceImplPackageAfterApi("service.mbp.impl")
                .build();
    }
}
