package site.yuanshen.data.enums;

import lombok.AllArgsConstructor;
import site.yuanshen.common.core.exception.GenshinApiException;

@AllArgsConstructor
public enum ScoreScopeEnum {
    PUNCTUATE;

    public static ScoreScopeEnum get(String code) {
        for (ScoreScopeEnum value : values())
            if (value.equals(code)) return value;
        throw new GenshinApiException("无效的统计类别: " + code);
    }
}
