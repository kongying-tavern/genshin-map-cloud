package site.yuanshen.data.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public enum HistoryType {

    AREA(1),
    ICON(2),
    ITEM(3),
    MARKER(4),
    TAG(5)
    ;

    @Getter
    private final Integer code;

    public static HistoryType from(Integer code) {
        return Arrays.stream(values()).filter(type -> type.getCode().equals(code)).findAny().orElseThrow(() -> new RuntimeException("类型[ " + code + " ]不在可选范围内"));
    }

}
