package site.yuanshen.data.enums.user.device;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserDeviceStatusEnum {
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
