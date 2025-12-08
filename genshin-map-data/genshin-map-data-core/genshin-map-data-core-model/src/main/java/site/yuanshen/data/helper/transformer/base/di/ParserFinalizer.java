package site.yuanshen.data.helper.transformer.base.di;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.config.DataConfig;
import site.yuanshen.data.helper.transformer.base.config.FinalizeConfig;

public final class ParserFinalizer {
    public String finalize(DataConfig dataConfig, FinalizeConfig finalizeConfig) {
        // Before Hook
        final Document doc = dataConfig.getDoc();
        finalizeConfig.invokeBeforeFinalizeHook(doc);

        String html = doc.body().html();

        // After Hook
        html = finalizeConfig.invokeAfterFinalizeHook(html);

        return html;
    }
}
