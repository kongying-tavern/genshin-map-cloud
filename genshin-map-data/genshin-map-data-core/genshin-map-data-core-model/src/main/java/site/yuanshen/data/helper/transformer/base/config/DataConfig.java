package site.yuanshen.data.helper.transformer.base.config;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;

public final class DataConfig {
    @Getter
    @Setter
    private String rawHtml = "";

    @Getter
    @Setter
    private String docHtml = "";

    @Getter
    @Setter
    private Document doc;
}
