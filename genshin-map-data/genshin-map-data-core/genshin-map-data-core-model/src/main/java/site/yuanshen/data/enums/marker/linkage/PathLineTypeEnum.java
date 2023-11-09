package site.yuanshen.data.enums.marker.linkage;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PathLineTypeEnum implements IEnum<String> {
    SOLID,
    DASHED,
    DOTTED;

    @Override
    public String getValue() {
        return this.name();
    }
}
