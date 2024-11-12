package site.yuanshen.data.enums.notice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.yuanshen.data.helper.unity.UnityRichTextHelper;

import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public enum NoticeTransformerEnum {
    UNITY(
            "Unity",
            UnityRichTextHelper::toTextMeshPro
    );

    private final String transformName;
    private final Function<String, String> contentTransformer;

    public static NoticeTransformerEnum find(String name) {
        for(NoticeTransformerEnum transformer : NoticeTransformerEnum.values()) {
            final String transName = transformer.transformName;
            if(Objects.equals(transName, name)) {
                return transformer;
            }
        }
        return null;
    }
}
