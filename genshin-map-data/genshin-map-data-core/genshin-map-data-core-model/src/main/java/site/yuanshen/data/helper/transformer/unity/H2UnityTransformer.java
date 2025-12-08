package site.yuanshen.data.helper.transformer.unity;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.parser.Parser;
import site.yuanshen.data.helper.transformer.base.HtmlBaseTransformer;
import site.yuanshen.data.helper.transformer.unity.extensions.*;

import java.util.regex.Pattern;

public class H2UnityTransformer extends HtmlBaseTransformer {
    public static String transform(String html) {
        final H2UnityTransformer runner = new H2UnityTransformer();
        return runner.process(html);
    }

    @Override
    public void configure() {
        this.configureLoad()
            .registerPreprocessor((String html) -> {
                return ReUtil.replaceAll(html, Pattern.compile("[\\r\\n]"), "");
            });
        this.configureSanitize()
            .addTag("p")
            .addTag("br")
            .addTag("b")
            .addTag("strong")
            .addTag("i")
            .addTag("em")
            .addTag("u")
            .addTag("size", "style")
            .addTag("color", "style")
            .addTag("a", "href")
            .addTag("link", "href")
            .addTag("ruby")
            .addTag("r")
            .addTag("rt");
        this.configureNormalize()
            .addTagMapping("b", "strong")
            .addTagMapping("i", "em")
            .addTagMapping("link", "a")
            .addTagMapping("r", "ruby");
        this.configureTransform()
            .registerExtension("color", new ColorExtension())
            .registerExtension("size", new SizeExtension())
            .registerExtension("r", new RubyExtension())
            .registerExtension("a", new AExtension())
            .registerExtension("p", new PExtension())
            .registerExtension("br", new BrExtension())
            .setOrders("r", "color", "size", "a", "p", "br");
        this.configureFinalize()
            .registerAfterFinalizeHook((html) -> {
                // Transform `color` and `size` values
                final Pattern valuePattern = Pattern.compile("\\s+collval\\s*=\\s*\"\\s*([^\"]+)\\s*\"");
                html = ReUtil.replaceAll(html, valuePattern, "=$1");
                // Transform HTML entities
                html = Parser.unescapeEntities(html, false);
                // Remove trailing new line due to `p` and `br`
                html = StrUtil.removeSuffix(html, "\n");
                return html;
            });
    }
}
