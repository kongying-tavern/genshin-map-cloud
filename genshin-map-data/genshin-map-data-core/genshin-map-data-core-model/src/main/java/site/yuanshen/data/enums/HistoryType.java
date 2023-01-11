package site.yuanshen.data.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum HistoryType {

    AREA(1),
    ICON(2),
    ITEM(3),
    MARKER(4),
    TAG(5)
    ;

    @Getter
    private final int code;

    public static HistoryType from(int code) {
        for (HistoryType value : values())
            if (value.code == code) return value;
        throw new RuntimeException("类型[ " + code + " ]不在可选范围内");
    }

}
