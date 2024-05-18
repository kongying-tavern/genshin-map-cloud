package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HistoryEditType {
    NONE(0),
    CREATE(1),
    UPDATE(2),
    DELETE(3),
    ;


    @JsonValue
    @EnumValue
    private final int value;

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

}
