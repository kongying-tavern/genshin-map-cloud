package site.yuanshen.data.helper.transformer.base.di;

import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.config.DataConfig;
import site.yuanshen.data.helper.transformer.base.config.LoadConfig;
import site.yuanshen.data.helper.transformer.base.config.OutputConfig;
import site.yuanshen.data.helper.transformer.base.config.SanitizeConfig;

public final class ParserLoader {
    private void preprocess(DataConfig dataConfig, LoadConfig loadConfig, String html) {
        html = StrUtil.blankToDefault(html, "");
        dataConfig.setRawHtml(html);
        html = loadConfig.invokePreprocessor(html);
        dataConfig.setDocHtml(html);
    }

    private void load(DataConfig dataConfig, OutputConfig outputConfig) {
        final String html = StrUtil.blankToDefault(dataConfig.getRawHtml(), "");
        final Document doc = Jsoup.parse(html);
        outputConfig.applySettings(doc);
        dataConfig.setDoc(doc);
    }

    private void sanitize(DataConfig dataConfig, SanitizeConfig sanitizeConfig) {
        Document doc = dataConfig.getDoc();
        doc = sanitizeConfig.invokeSanitize(doc);
        dataConfig.setDoc(doc);
    }

    public void execute(
        DataConfig dataConfig,
        OutputConfig outputConfig,
        LoadConfig loadConfig,
        SanitizeConfig sanitizeConfig,
        String html
    ) {
        this.preprocess(dataConfig, loadConfig, html);
        this.load(dataConfig, outputConfig);
        this.sanitize(dataConfig, sanitizeConfig);
    }
}
