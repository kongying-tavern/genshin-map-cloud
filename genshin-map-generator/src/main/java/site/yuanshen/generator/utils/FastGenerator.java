package site.yuanshen.generator.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import site.yuanshen.data.base.BaseEntity;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    private String dtoPackage;
    private String voPackage;

    /**
     * 在创建实体类属性的swagger注解时创建注释
     */
    private static final Boolean enableFieldCommentWithSwagger = true;

    public void build() {
        System.out.println("Output Dir: " + outputDir);

        FastAutoGenerator.create(new DataSourceConfig.Builder(url,userName,password)
                        //3.5.3之后，默认为DefaultQuery，会使得pg的json数据被识别为object，且无法被mbp的转化器转化
                        .databaseQueryClass(SQLQuery.class)
                        .schema("genshin_map")
                        .dbQuery(new PostgreSqlQuery())
                        .typeConvert(new PostgreSqlTypeConvert()))
                //全局配置
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(System.getProperty("user.dir") + outputDir)
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
                        .xml(xmlPackage))
                .strategyConfig(builder -> builder
                        // 跳过视图
                        .enableSkipView()
                        /*-------------entity配置-------------*/
                        .entityBuilder()
                        //启用lombok
                        .enableLombok()
                        //生成字段关联注解
                        .enableTableFieldAnnotation()
                        //BaseEntity中已经实现Serialize接口
                        .disableSerialVersionUID()
                        //entity公共父类设置
                        .superClass(BaseEntity.class)
                        .versionPropertyName("version")
                        .addTableFills(new Property("version", FieldFill.INSERT))
                        .addSuperEntityColumns("create_time", "update_time", "creator_id", "updater_id", "del_flag")
                        .enableFileOverride()
                        /*-------------service配置-------------*/
                        .serviceBuilder()
                        .formatServiceFileName("%sMBPService")
                        .formatServiceImplFileName("%sMBPServiceImpl")
                        .enableFileOverride()
                        /*-------------mapper配置-------------*/
                        .mapperBuilder()
                        .mapperAnnotation(Mapper.class)
                        .enableFileOverride()
                        .enableBaseColumnList()
                        .enableBaseResultMap()
                        .convertXmlFileName(tableName -> "MBP"+tableName+"Mapper"))
                .templateConfig(builder -> builder
                        .disable(TemplateType.CONTROLLER)
                        .entity("/templates/entity.java")
                        .service("/templates/service.java")
                        .serviceImpl("/templates/serviceImpl.java")
                        .mapper("/templates/mapper.java")
                        .xml("/templates/mapper.xml"))
                .injectionConfig(builder -> builder
                        .customFile(fileBuilder -> fileBuilder
                                .fileName("Dto.java")
                                .templatePath("/templates/dto.java.ftl")
                                .packageName(dtoPackage)
                                .enableFileOverride())
                        .customFile(fileBuilder -> fileBuilder
                                .fileName("Vo.java")
                                .templatePath("/templates/vo.java.ftl")
                                .packageName(voPackage)
                                .enableFileOverride()))
                .templateEngine(new FreemarkerTemplateEngine())
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

    public FastGenerator dtoPackage(String dtoPackage) {
        this.dtoPackage = dtoPackage;
        return this;
    }

    public FastGenerator voPackage(String voPackage) {
        this.voPackage = voPackage;
        return this;
    }
}
