package site.yuanshen.data.helper.transformer.base;

import site.yuanshen.data.helper.transformer.base.config.*;
import site.yuanshen.data.helper.transformer.base.di.ParserFinalizer;
import site.yuanshen.data.helper.transformer.base.di.ParserLoader;
import site.yuanshen.data.helper.transformer.base.di.ParserTransformer;

public abstract class HtmlBaseTransformer {
    private boolean isConfigured = false;
    private final DataConfig dataConfig = new DataConfig();
    private final OutputConfig outputConfig = new OutputConfig();
    private final LoadConfig loadConfig = new LoadConfig();
    private final SanitizeConfig sanitizeConfig = new SanitizeConfig();
    private final NormalizeConfig normalizeConfig = new NormalizeConfig();
    private final TransformConfig transformConfig = new TransformConfig();
    private final FinalizeConfig finalizeConfig = new FinalizeConfig();

    private final ParserLoader loader = new ParserLoader();
    private final ParserTransformer transformer = new ParserTransformer();
    private final ParserFinalizer finalizer = new ParserFinalizer();

    public abstract void configure();

    public final OutputConfig configureOutput() {
        return this.outputConfig;
    }

    public final LoadConfig configureLoad() {
        return this.loadConfig;
    }

    public final SanitizeConfig configureSanitize() {
        return this.sanitizeConfig;
    }

    public final NormalizeConfig configureNormalize() {
        return this.normalizeConfig;
    }

    public final TransformConfig configureTransform() {
        return this.transformConfig;
    }

    public final FinalizeConfig configureFinalize() {
        return this.finalizeConfig;
    }

    private void internalConfigure(boolean force) {
        if (force) {
            configure();
            this.isConfigured = true;
        } else if (!this.isConfigured) {
            configure();
            this.isConfigured = true;
        }
    }

    public final String process(String html) {
        this.internalConfigure(false);
        // Load
        this.loader.execute(
            this.dataConfig,
            this.outputConfig,
            this.loadConfig,
            this.sanitizeConfig,
            html
        );
        this.transformer.normalize(
            this.dataConfig,
            this.normalizeConfig
        );
        this.transformer.transform(
            this.dataConfig,
            this.transformConfig
        );

        html = this.finalizer.finalize(
            this.dataConfig,
            this.finalizeConfig
        );

        return html;
    }
}
