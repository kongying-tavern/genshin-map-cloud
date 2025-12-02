package site.yuanshen.data.helper.transformer.base.config;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;

import java.util.*;

public final class NormalizeConfig {
    private Map<String, Set<String>> normalizeTags = new HashMap<>();

    /**
     * 添加标签映射
     * @param replaceTagName 替换标签名
     * @param findTagName 匹配标签名
     */
    public NormalizeConfig addTagMapping(String replaceTagName, String... findTagName) {
        if (StrUtil.isBlank(replaceTagName)) {
            return this;
        }

        final Set<String> allowedAttributes = new HashSet<>(Arrays.asList(findTagName));
        this.normalizeTags.put(replaceTagName, allowedAttributes);

        return this;
    }

    public NormalizeConfig removeTagMapping(String tagName) {
        if (StrUtil.isBlank(tagName)) {
            return this;
        }

        this.normalizeTags.remove(tagName);
        return this;
    }

    public void applyNormalizer(Document doc) {
        if (doc == null) {
            return;
        }

        this.normalizeTags.forEach((replaceTagName, findTagNames) -> {
            if (StrUtil.isBlank(replaceTagName)) {
                return;
            } else if (findTagNames.isEmpty()) {
                return;
            }

            String findTagLabel = String.join(", ", findTagNames);
            doc.select(findTagLabel).tagName(replaceTagName);
        });
    }
}
