package site.yuanshen.data.helper.transformer.base.config;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;

import java.util.function.Consumer;
import java.util.function.Function;

public final class FinalizeConfig {
    private Consumer<Document> beforeFinalize = null;

    private Function<String, String> afterFinalize = null;

    public FinalizeConfig registerBeforeFinalizeHook(Consumer<Document> beforeFinalize) {
        if (beforeFinalize == null) {
            return this;
        }

        this.beforeFinalize = beforeFinalize;
        return this;
    }

    public FinalizeConfig unregisterBeforeFinalizeHook() {
        this.beforeFinalize = null;
        return this;
    }

    public void invokeBeforeFinalizeHook(Document doc) {
        if (doc == null) {
            return;
        }

        if (this.beforeFinalize != null) {
            this.beforeFinalize.accept(doc);
        }
    }

    public FinalizeConfig registerAfterFinalizeHook(Function<String, String> afterFinalize) {
        if (afterFinalize == null) {
            return this;
        }

        this.afterFinalize = afterFinalize;
        return this;
    }

    public FinalizeConfig unregisterAfterFinalizeHook() {
        this.afterFinalize = null;
        return this;
    }

    public String invokeAfterFinalizeHook(String html) {
        html = StrUtil.blankToDefault(html, "");
        if (this.afterFinalize != null) {
            html = this.afterFinalize.apply(html);
        }

        return html;
    }
}
