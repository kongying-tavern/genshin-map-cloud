package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeviceStatusEnum {
    UNKNOWN(0, "未知"),
    VALID(1, "有效"),
    BLOCKED(2, "禁用")
    ;

    @Getter
    @EnumValue
    private final Integer code;

    @Getter
    private final String name;

}
