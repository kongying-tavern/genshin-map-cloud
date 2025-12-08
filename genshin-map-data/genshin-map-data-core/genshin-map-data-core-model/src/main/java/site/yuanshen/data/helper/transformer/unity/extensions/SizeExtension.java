package site.yuanshen.data.helper.transformer.unity.extensions;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;
import site.yuanshen.data.helper.transformer.base.utils.HtmlParseUtils;

import java.util.Map;

public final class SizeExtension implements ExtensionInterface {
    @Override
    public void transform(Document doc) {
        doc
            .select("size")
            .forEach(el -> {
                final Attribute style = el.attribute("style");
                final Map<String, String> styleAttrs = HtmlParseUtils.getStyleAttrs(style);
                final String size = styleAttrs.getOrDefault("--size", "");

                el.clearAttributes();
                String sizeValue = "";
                if (StrUtil.isNotBlank(size)) {
                    sizeValue = HtmlParseUtils.sizeToNumber(size);
                }
                if (StrUtil.isBlank(sizeValue)) {
                    el.unwrap();
                } else {
                    el.attr("collval", sizeValue);
                }
            });
    }
}
