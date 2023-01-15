package site.yuanshen.generator.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import org.springframework.stereotype.Component;
import site.yuanshen.data.base.BaseEntity;

import java.util.Map;
import java.util.TreeMap;

@Component

public class FastGenerator {
    private String url;
    private String userName;
    private String password;
    private String author;
    private String outputDir;
    private String commentDateFormat;

    private String apiPackageName;
    private String apiModuleName;
    private String entityPackage;
    private String servicePackage;
    private String serviceImplPackage;
    private String mapperPackage;
    private String xmlPackage;
    private String otherPackage;

    /**
     * 在创建实体类属性的swagger注解时创建注释
     */
    private static final Boolean enableFieldCommentWithSwagger = true;

    public void build() {
        System.out.println("Output Dir: " + outputDir);

        FastAutoGenerator.create(url, userName, password)
                //全局配置
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(System.getProperty("user.dir") + outputDir)
                        //启用swagger
                        //.enableSwagger()
                        //关闭生成后自动打开文件夹
                        .disableOpenDir()
                        .commentDate(commentDateFormat))
                .packageConfig(builder -> builder
                        .parent("")
                        .moduleName("")
                        .entity(entityPackage)
                        .service(servicePackage)
                        .serviceImpl(serviceImplPackage)
                        .mapper(mapperPackage)
                        .other(otherPackage)
                        .xml(xmlPackage))
                .strategyConfig(builder -> builder
                        // 跳过视图
                        .enableSkipView()
                        //entity配置
                        .entityBuilder()
                        //启用lombok
                        .enableLombok()
                        //生成字段关联注解
                        .enableTableFieldAnnotation()
                        //BaseEntity中已经实现Serialize接口
                        .disableSerialVersionUID()
                        //entity公共父类设置
                        .superClass(BaseEntity.class)
                        .addSuperEntityColumns("version", "create_time", "update_time", "creator_id", "updater_id", "del_flag")

                        //service配置
                        .serviceBuilder()
                        .formatServiceFileName("%sMBPService")
                        .formatServiceImplFileName("%sMBPServiceImpl")

                        //mapper配置
                        .mapperBuilder()
                        .enableMapperAnnotation())
                .templateConfig(builder -> builder
                        .disable(TemplateType.CONTROLLER, TemplateType.SERVICEIMPL)
                        .entity("/templates/entity.java")
                        .service("/templates/service.java")
                        .serviceImpl("/templates/serviceImpl.java")
                        .mapper("/templates/mapper.java")
                        .xml("/templates/mapper.xml"))
                .injectionConfig(builder -> {
                    Map<String, Object> customFieldMap = new TreeMap<>();
                    customFieldMap.put("enableFieldCommentWithSwagger", enableFieldCommentWithSwagger);
                    builder.customMap(customFieldMap);
                    Map<String, String> customFileMap = new TreeMap<>();
                    customFileMap.put("Dto.java", "/templates/dto.java.vm");
                    customFileMap.put("Vo.java", "/templates/vo.java.vm");
                    builder.customFile(customFileMap);
                })
                .execute();
    }

    public static FastGenerator getFastGenerator() {
        return new FastGenerator();
    }

    public FastGenerator url(String url) {
        this.url = url;
        return this;
    }

    public FastGenerator userName(String userName) {
        this.userName = userName;
        return this;
    }

    public FastGenerator password(String password) {
        this.password = password;
        return this;
    }

    public FastGenerator author(String author) {
        this.author = author;
        return this;
    }

    public FastGenerator outputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public FastGenerator commentDateFormat(String commentDateFormat) {
        this.commentDateFormat = commentDateFormat;
        return this;
    }

    public FastGenerator entityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
        return this;
    }

    public FastGenerator mapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
        return this;
    }

    public FastGenerator xmlPackage(String xmlPackage) {
        this.xmlPackage = xmlPackage;
        return this;
    }

    public FastGenerator apiPackageName(String apiPackageName) {
        this.apiPackageName = apiPackageName;
        return this;
    }

    public FastGenerator apiModuleName(String apiModuleName) {
        this.apiModuleName = apiModuleName;
        return this;
    }

    public FastGenerator servicePackageAfterApi(String servicePackageAfterApi) throws Exception {
        if (apiPackageName == null) throw new Exception("serviceImplPackageAfterApi前必须设置apiPackageName");
        if (apiModuleName == null) throw new Exception("serviceImplPackageAfterApi前必须设置apiModuleName");
        this.servicePackage = apiPackageName + "." + apiModuleName + "." + servicePackageAfterApi;
        return this;
    }

    public FastGenerator serviceImplPackageAfterApi(String serviceImplPackageAfterApi) throws Exception {
        if (apiPackageName == null) throw new Exception("serviceImplPackageAfterApi前必须设置apiPackageName");
        if (apiModuleName == null) throw new Exception("serviceImplPackageAfterApi前必须设置apiModuleName");
        this.serviceImplPackage = apiPackageName + "." + apiModuleName + "." + serviceImplPackageAfterApi;
        return this;
    }

    public FastGenerator otherPackage(String otherPackage) {
        this.otherPackage = otherPackage;
        return this;
    }
}
