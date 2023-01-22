package site.yuanshen.data.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ScoreScopeEnum {
    PUNCTUATE;

    public static ScoreScopeEnum get(String code) {
        for (ScoreScopeEnum value : values())
            if (value.equals(code)) return value;
        throw new RuntimeException("无效的统计类别: " + code);
    }
}
