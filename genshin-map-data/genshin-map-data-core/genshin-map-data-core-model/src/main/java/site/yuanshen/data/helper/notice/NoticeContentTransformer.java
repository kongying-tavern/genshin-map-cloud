package site.yuanshen.data.helper.notice;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import java.util.Map;
import java.util.regex.Pattern;

public final class NoticeContentTransformer {
    public static String convertUnity(String str) {
        String html = StrUtil.nullToEmpty(str);
        html = ReUtil.replaceAll(html, Pattern.compile("[\\r\\n]"), "");
        Document doc = Jsoup.parse(html);

        final Safelist safeList = (new Safelist())
                .addTags(
                        "p", "br",
                        "b", "strong",
                        "i", "em",
                        "u",
                        "size",
                        "color",
                        "a"
                )
                .addAttributes("size", "style")
                .addAttributes("color", "style")
                .addAttributes("a", "href");
        Cleaner safeCleaner = new Cleaner(safeList);
        doc = safeCleaner.clean(doc);
        doc.outputSettings(
                (new Document.OutputSettings())
                        .prettyPrint(false)
                        .charset(DataUtil.UTF_8)
                        .escapeMode(Entities.EscapeMode.xhtml)
                        .syntax(Document.OutputSettings.Syntax.xml)
        );

        // Replace `strong` to `b`, `em` to `i`
        doc.select("strong").tagName("b");
        doc.select("em").tagName("i");

        // Re-format `color` and `size`
        doc
                .select("color")
                .replaceAll(el -> {
                    final Attribute style = el.attribute("style");
                    final Map<String, String> styleAttrs = NoticeDataHelper.getStyleAttrs(style);
                    final String color = styleAttrs.getOrDefault("--color", "");

                    el.clearAttributes();
                    String colorValue = "";
                    if(StrUtil.isNotBlank(color)) {
                        colorValue = NoticeDataHelper.colorToHex(color);
                    }
                    if(StrUtil.isNotBlank(colorValue)) {
                        el.attr("collval", colorValue);
                    }
                    return el;
                });
        doc
                .select("size")
                .replaceAll(el -> {
                    final Attribute style = el.attribute("style");
                    final Map<String, String> styleAttrs = NoticeDataHelper.getStyleAttrs(style);
                    final String size = styleAttrs.getOrDefault("--size", "");

                    el.clearAttributes();
                    String sizeValue = "";
                    if(StrUtil.isNotBlank(size)) {
                        sizeValue = NoticeDataHelper.sizeToNumber(size);
                    }
                    if(StrUtil.isNotBlank(sizeValue)) {
                        el.attr("collval", sizeValue);
                    }
                    return el;
                });

        // Re-format `a` to `link`
        doc
                .select("a, link")
                .tagName("link")
                .replaceAll(el -> {
                    final Attribute hrefAttr = el.attribute("href");
                    final String href = hrefAttr.getValue();
                    el.clearAttributes();
                    if(StrUtil.isBlank(href)) {
                        el.unwrap();
                    } else {
                        el.attr("collval", href);
                    }
                    return el;
                });

        // Replace `p` and `br` with new lines
        doc
                .select("p, br")
                .append("\n")
                .unwrap();

        html = doc.body().html();
        // Transform `color` and `size` values
        final Pattern valuePattern = Pattern.compile("\\s+collval\\s*=\\s*\"\\s*([^\"]+)\\s*\"");
        html = ReUtil.replaceAll(html, valuePattern, "=$1");
        // Transform HTML entities
        html = Parser.unescapeEntities(html, false);
        // Remove trailing new line due to `p` and `br`
        html = StrUtil.removeSuffix(html, "\n");

        return html;
    }
}
