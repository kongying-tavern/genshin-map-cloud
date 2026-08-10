package site.yuanshen.data.helper.transformer.unity.extensions;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;

public final class AExtension implements ExtensionInterface {
    @Override
    public void transform(Document doc) {
        doc
            .select("link")
            .forEach(el -> {
                final Attribute hrefAttr = el.attribute("href");
                final String href = hrefAttr.getValue();
                el.clearAttributes();
                if (StrUtil.isBlank(href)) {
                    el.unwrap();
                } else {
                    el.attr("collval", href);
                }
            });
    }
}
