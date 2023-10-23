package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import site.yuanshen.common.core.exception.GenshinApiException;

/**
 * 打点操作类型枚举
 *
 * @author Moment
 */
public enum PunctuateMethodEnum {

    ADD(1),
    UPDATE(2),
    DELETE(3);


    @EnumValue
    @Getter
    private final int typeCode;

    PunctuateMethodEnum(int typeCode) {
        this.typeCode = typeCode;
    }

    public static PunctuateMethodEnum from(Integer typeCode) {
        if (typeCode == null) throw new GenshinApiException("无打点操作类型，请联系管理员");
        for (PunctuateMethodEnum value : values())
            if (value.typeCode == typeCode) return value;
        throw new GenshinApiException("不支持的打点操作类型，请联系管理员检查参数");
    }
}
