package site.yuanshen.data.enums.transformer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.yuanshen.data.helper.transformer.unity.H2UnityTransformer;

import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public enum HtmlTransformerEnum {
    UNITY(
        "Unity",
        H2UnityTransformer::transform
    );

    private final String transformName;
    private final Function<String, String> contentTransformer;

    public static HtmlTransformerEnum find(String name) {
        for (HtmlTransformerEnum transformer : HtmlTransformerEnum.values()) {
            final String transName = transformer.transformName;
            if (Objects.equals(transName, name)) {
                return transformer;
            }
        }
        return null;
    }
}
