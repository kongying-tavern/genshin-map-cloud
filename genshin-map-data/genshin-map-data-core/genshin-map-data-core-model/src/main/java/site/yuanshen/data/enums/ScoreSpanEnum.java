package site.yuanshen.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ScoreSpanEnum {
    DAY;

    public static ScoreSpanEnum get(String code) {
        for (ScoreSpanEnum value : values())
            if(value.equals(code)) return value;
        throw new RuntimeException("无效的统计颗粒度: " + code);
    }
}
