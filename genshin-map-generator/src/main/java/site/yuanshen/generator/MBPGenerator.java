package site.yuanshen.generator;

import site.yuanshen.generator.utils.FastGenerator;

/**
 * MybatisPlus 代码生成器
 *
 * @author Moment
 */
public class MBPGenerator {

    public static void main(String[] args) throws Exception {
        FastGenerator generator = FastGenerator.getFastGenerator()
                .url("jdbc:mysql://localhost:3306/genshin_map?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai")
                .userName("root")
                .password("momincong")
                .author("Moment")
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
