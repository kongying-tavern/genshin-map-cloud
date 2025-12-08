package site.yuanshen.data.helper.transformer.unity.extensions;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import site.yuanshen.data.helper.transformer.base.extensions.ExtensionInterface;

import java.util.List;
import java.util.stream.Collectors;

public final class RubyExtension implements ExtensionInterface {
    @Override
    public void transform(Document doc) {
        doc
            .select("r")
            .forEach(el -> {
                final Element elClone = el.clone();

                // Get definition
                final Elements defList = elClone.select("rt");
                final List<String> defContentList = defList.stream()
                    .map(Element::html)
                    .collect(Collectors.toList());
                final String defContent = StrUtil.blankToDefault(StrUtil.join("", defContentList), "");
                defList.remove();

                // Get contents
                final String mainContent = StrUtil.blankToDefault(elClone.html(), "");

                // Rebuild ruby element
                final Element newEl = new Element("r");
                newEl.html(mainContent);
                if (StrUtil.isBlank(defContent)) {
                    newEl.html(mainContent);
                } else {
                    final String combinedContent = StrUtil.format("{}<rt>{}</rt>", mainContent, defContent);
                    newEl.html(combinedContent);
                }

                el.replaceWith(newEl);
            });
    }
}
