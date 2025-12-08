package site.yuanshen.data.helper.transformer.base.config;

import lombok.Getter;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.nio.charset.Charset;

public final class OutputConfig {
    @Getter
    private boolean prettyPrint = false;

    public OutputConfig setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    @Getter
    private Charset charset = DataUtil.UTF_8;

    public OutputConfig setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Getter
    private Entities.EscapeMode escapeMode = Entities.EscapeMode.xhtml;

    public OutputConfig setEscapeMode(Entities.EscapeMode escapeMode) {
        this.escapeMode = escapeMode;
        return this;
    }

    @Getter
    private Document.OutputSettings.Syntax syntax = Document.OutputSettings.Syntax.xml;

    public OutputConfig setSyntax(Document.OutputSettings.Syntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public Document.OutputSettings toOutputSettings() {
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings
            .prettyPrint(this.prettyPrint)
            .charset(this.charset)
            .escapeMode(this.escapeMode)
            .syntax(this.syntax);
        return outputSettings;
    }

    public void applySettings(Document doc) {
        if (doc == null) {
            return;
        }

        doc.outputSettings(this.toOutputSettings());
    }
}
