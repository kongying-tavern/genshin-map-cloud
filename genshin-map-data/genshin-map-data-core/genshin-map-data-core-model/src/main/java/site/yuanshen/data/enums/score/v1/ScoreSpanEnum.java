package site.yuanshen.data.enums.score.v1;

import lombok.AllArgsConstructor;
import site.yuanshen.common.core.exception.GenshinApiException;

@AllArgsConstructor
public enum ScoreSpanEnum {
    DAY;

    public static ScoreSpanEnum get(String code) {
        for (ScoreSpanEnum value : values())
            if(value.equals(code)) return value;
        throw new GenshinApiException("无效的统计颗粒度: " + code);
    }
}
