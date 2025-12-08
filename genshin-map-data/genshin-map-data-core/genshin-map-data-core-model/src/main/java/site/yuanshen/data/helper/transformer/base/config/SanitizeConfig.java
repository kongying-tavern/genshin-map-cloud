package site.yuanshen.data.helper.transformer.base.config;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import java.util.*;

public final class SanitizeConfig {
    private Map<String, Set<String>> allowedTags = new HashMap<>();

    /**
     * 添加标签，
     * addTag("div", ":all")，不进行属性过滤
     * addTag("div", "class", "style")，进行属性过滤
     * addTag("div")，过滤掉所有属性
     */
    public SanitizeConfig addTag(String tagName, String... allowedAttribute) {
        if (StrUtil.isBlank(tagName)) {
            return this;
        }

        final Set<String> allowedAttributes = new HashSet<>(Arrays.asList(allowedAttribute));
        this.allowedTags.put(tagName, allowedAttributes);

        return this;
    }

    public SanitizeConfig removeTag(String tagName) {
        if (StrUtil.isBlank(tagName)) {
            return this;
        }

        this.allowedTags.remove(tagName);
        return this;
    }

    public Document invokeSanitize(Document doc) {
        if (doc == null) {
            return null;
        }

        Safelist safeList = (new Safelist());
        this.allowedTags.forEach((tagName, allowedAttributes) -> {
            if (StrUtil.isBlank(tagName)) {
                return;
            }

            if (allowedAttributes.isEmpty()) {
                safeList.addTags(tagName);
            } else {
                safeList.addAttributes(tagName, allowedAttributes.toArray(new String[0]));
            }

        });

        final Cleaner safeCleaner = new Cleaner(safeList);
        final Document document = safeCleaner.clean(doc);
        return document;
    }
}
