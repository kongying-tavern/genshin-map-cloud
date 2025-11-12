package site.yuanshen.data.helper.transformer.base.di;

import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.config.DataConfig;
import site.yuanshen.data.helper.transformer.base.config.NormalizeConfig;
import site.yuanshen.data.helper.transformer.base.config.TransformConfig;

public final class ParserTransformer {
    public void normalize(DataConfig dataConfig, NormalizeConfig normalizeConfig) {
        final Document doc = dataConfig.getDoc();
        normalizeConfig.applyNormalizer(doc);
    }

    public void transform(
        DataConfig dataConfig,
        TransformConfig transformConfig
    ) {
        final Document doc = dataConfig.getDoc();
        transformConfig.applyTransformers(doc);
    }
}
