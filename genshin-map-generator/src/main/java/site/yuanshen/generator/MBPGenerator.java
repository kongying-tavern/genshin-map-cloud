package site.yuanshen.generator;

import site.yuanshen.generator.utils.FastGenerator;

/**
 * MybatisPlus 代码生成器
 *
 * @author Moment
 */
public class MBPGenerator {
    private static String url = System.getenv("GSAPI_DB_URL");

    private static String username = System.getenv("GSAPI_DB_USER");

    private static String password = System.getenv("GSAPI_DB_PASS");

    private static String author = System.getenv("GSAPI_AUTHOR");

    public static void main(String[] args) throws Exception {
        System.out.println(url);
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
                .otherPackage("dto&vo")
                .apiPackageName("site.yuanshen.api")
                .apiModuleName("core")
                .servicePackageAfterApi("service.mbp")
                .serviceImplPackageAfterApi("service.mbp.impl")
                .build();
    }
}
