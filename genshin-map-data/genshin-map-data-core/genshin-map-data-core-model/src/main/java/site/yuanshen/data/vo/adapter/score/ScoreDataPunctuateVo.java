package site.yuanshen.data.vo.adapter.score;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScoreDataPunctuateVo {
    private Map<String, Integer> fields = new HashMap<>();
    private Map<String, Integer> chars = new HashMap<>();

    public void merge(ScoreDataPunctuateVo data) {
        if(data == null)
            return;
        if(this.fields == null)
            this.fields = new HashMap<>();
        if(this.chars == null)
            this.chars = new HashMap<>();

        Map<String, Integer> newFields = data.getFields();
        if(newFields == null)
            newFields = new HashMap<>();
        Map<String, Integer> newChars = data.getChars();
        if(newChars == null)
            newChars = new HashMap<>();

        newFields.forEach((fieldName, fieldVal) -> {
            this.fields.compute(fieldName, (k, v) -> v == null ? fieldVal : v + fieldVal);
        });
        newChars.forEach((charName, charVal) -> {
            this.chars.compute(charName, (k, v) -> v == null ? charVal : v + charVal);
        });
    }
}
