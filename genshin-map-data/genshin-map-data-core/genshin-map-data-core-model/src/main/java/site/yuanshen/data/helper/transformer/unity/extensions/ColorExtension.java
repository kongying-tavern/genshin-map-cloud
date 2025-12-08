package site.yuanshen.data.helper.transformer.unity.extensions;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;
import site.yuanshen.data.helper.transformer.base.utils.HtmlParseUtils;

import java.util.Map;

public final class ColorExtension implements ExtensionInterface {
    @Override
    public void transform(Document doc) {
        doc
            .select("color")
            .forEach(el -> {
                final Attribute style = el.attribute("style");
                final Map<String, String> styleAttrs = HtmlParseUtils.getStyleAttrs(style);
                final String color = styleAttrs.getOrDefault("--color", "");

                el.clearAttributes();
                String colorValue = "";
                if (StrUtil.isNotBlank(color)) {
                    colorValue = HtmlParseUtils.colorToHex(color);
                }
                if (StrUtil.isBlank(colorValue)) {
                    el.unwrap();
                } else {
                    el.attr("collval", colorValue);
                }
            });
    }
}
