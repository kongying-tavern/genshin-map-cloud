package site.yuanshen.common.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;

import java.util.Map;

public class TemplateUtils {
    public static String execTemplate(String template, Map<String, Object> params) {
        template = StrUtil.blankToDefault(template, "");
        TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig());
        Template tpl = engine.getTemplate(template);
        String result = tpl.render(params);
        return result;
    }
}
