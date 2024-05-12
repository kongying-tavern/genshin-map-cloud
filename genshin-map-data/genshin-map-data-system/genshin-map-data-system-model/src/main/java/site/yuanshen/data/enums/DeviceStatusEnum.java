package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeviceStatusEnum {
    UNKNOWN(0, "未知"),
    VALID(1, "有效"),
    BLOCKED(2, "禁用")
    ;

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

}
