package site.yuanshen.data.enums.marker.linkage;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PathArrowTypeEnum implements IEnum<String> {
    NONE,
    ARROW,
    CIRCLE,
    DOT;

    @Override
    public String getValue() {
        return this.name();
    }
}
