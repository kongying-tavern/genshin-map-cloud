package site.yuanshen.data.helper.transformer.unity.extensions;

import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;

public final class BrExtension implements ExtensionInterface {
    @Override
    public void transform(Document doc) {
        doc
            .select("br")
            .append("\n")
            .unwrap();
    }
}
