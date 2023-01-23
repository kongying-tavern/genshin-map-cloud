package site.yuanshen.data.vo.adapter.score;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScoreDataVo {
    private Map<String, Integer> fields = new HashMap<>();
    private Map<String, Integer> chars = new HashMap<>();
}
