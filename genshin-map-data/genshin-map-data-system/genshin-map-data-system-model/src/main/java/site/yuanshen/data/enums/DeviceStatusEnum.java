package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeviceStatusEnum implements IEnum<Integer> {
    UNKNOWN(0, "未知"),
    VALID(1, "有效"),
    BLOCKED(2, "禁用")
    ;

    @EnumValue
    @JsonValue
    @Getter
    private final Integer code;

    @Getter
    private final String name;

    @Override
    public Integer getValue() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.code.toString();
    }
}
