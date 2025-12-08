package site.yuanshen.data.helper.transformer.base.config;

import cn.hutool.core.util.StrUtil;

import java.util.function.Function;

public final class LoadConfig {
    private Function<String, String> preprocessor = null;

    public LoadConfig registerPreprocessor(Function<String, String> preprocessor) {
        if (preprocessor == null) {
            // Do Nothing
            return this;
        }
        this.preprocessor = preprocessor;

        return this;
    }

    public LoadConfig unregisterPreprocessor() {
        this.preprocessor = null;
        return this;
    }

    public String invokePreprocessor(String html) {
        html = StrUtil.blankToDefault(html, "");
        if (this.preprocessor != null) {
            html = this.preprocessor.apply(html);
        }
        return html;
    }
}
