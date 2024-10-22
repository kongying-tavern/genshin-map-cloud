package site.yuanshen.data.vo.adapter.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class ItemListCacheKey {
    private String md5 = "";

    private int hiddenFlag = -1;

    private int index = -1;
}
