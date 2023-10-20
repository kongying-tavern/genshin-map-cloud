package site.yuanshen.generator.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import site.yuanshen.data.base.BaseEntity;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component

public class FastGenerator {
    private String url;
    private String schema;
    private String userName;
    private String password;
    private String author;
    private String entity;
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

    private static final String defaultSchema = "genshin_map";

    public void build() {
        System.out.println("Output Dir: " + outputDir);
        final Map<String, String> customPathMap = this.getCustomPathMap();

        FastAutoGenerator.create(new DataSourceConfig.Builder(url,userName,password)
                        //3.5.3之后，默认为DefaultQuery，会使得pg的json数据被识别为object，且无法被mbp的转化器转化
                        .databaseQueryClass(SQLQuery.class)
                        .schema(schema)
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
                        .xml(xmlPackage)
                        .pathInfo(this.getPathMap())
                )
                .strategyConfig(builder -> builder
                        // 添加需要生成模块的白名单列表
                        .addInclude(StrUtil.split(entity, ","))
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
                        .addTableFills(new Property("version", FieldFill.INSERT_UPDATE))
                        .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))
                        .addTableFills(new Property("updaterId", FieldFill.INSERT_UPDATE))
                        .addSuperEntityColumns("create_time", "creator_id", "del_flag")
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
                                .filePath(customPathMap.get("dto"))
                                .enableFileOverride())
                        .customFile(fileBuilder -> fileBuilder
                                .fileName("Vo.java")
                                .templatePath("/templates/vo.java.ftl")
                                .packageName(voPackage)
                                .filePath(customPathMap.get("vo"))
                                .enableFileOverride()))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    private Map<OutputFile, String> getPathMap() {
        final String outputBase = System.getProperty("user.dir") + outputDir;
        Map<OutputFile, String> pathMap = new HashMap<>();

        pathMap.put(OutputFile.entity, outputBase + File.separator + "genshin-map-data/genshin-map-data-core/genshin-map-data-core-model/src/main/java");
        pathMap.put(OutputFile.service, outputBase + File.separator + "genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/src/main/java");
        pathMap.put(OutputFile.serviceImpl, outputBase + File.separator + "genshin-map-api/genshin-map-api-core/genshin-map-api-core-core/src/main/java");
        pathMap.put(OutputFile.mapper, outputBase + File.separator + "genshin-map-data/genshin-map-data-core/genshin-map-data-core-mapper/src/main/java");
        pathMap.put(OutputFile.xml, outputBase + File.separator + "genshin-map-data/genshin-map-data-core/genshin-map-data-core-mapper/src/main/resources");

        return pathMap;
    }

    private Map<String, String> getCustomPathMap() {
        final String outputBase = System.getProperty("user.dir") + outputDir;
        Map<String, String> pathMap = new HashMap<>();

        pathMap.put("dto", outputBase + File.separator + "genshin-map-data/genshin-map-data-core/genshin-map-data-core-model/src/main/java");
        pathMap.put("vo", outputBase + File.separator + "genshin-map-data/genshin-map-data-core/genshin-map-data-core-model/src/main/java");

        return pathMap;
    }

    public static FastGenerator getFastGenerator() {
        return new FastGenerator();
    }

    public FastGenerator url(String url) {
        this.url = url;
        this.schema = this.getUrlSchema();
        return this;
    }

    private String getUrlSchema() {
        try {
            URI jdbcUri = new URI(this.url);
            UriComponents jdbcUriComponents = UriComponentsBuilder
                    .fromUri(jdbcUri)
                    .encode(StandardCharsets.UTF_8)
                    .build();
            String jdbcUriSsp = StrUtil.blankToDefault(jdbcUriComponents.getSchemeSpecificPart(), "");
            UriComponents dbUriComponents = UriComponentsBuilder
                    .fromUriString(jdbcUriSsp)
                    .encode(StandardCharsets.UTF_8)
                    .build();

            MultiValueMap<String, String> queryMap = dbUriComponents.getQueryParams();
            String querySchema = queryMap.getFirst("currentSchema");
            String querySchemaStr = StrUtil.blankToDefault(querySchema, defaultSchema);
            return querySchemaStr;
        } catch (Exception e) {
            return defaultSchema;
        }
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

    public FastGenerator entity(String entity) {
        this.entity = entity;
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
