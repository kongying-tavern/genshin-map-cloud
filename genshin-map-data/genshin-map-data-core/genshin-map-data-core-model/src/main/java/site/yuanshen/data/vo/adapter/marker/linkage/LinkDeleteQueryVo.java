package site.yuanshen.data.vo.adapter.marker.linkage;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LinkDeleteQueryVo implements Serializable {
    public List<Long> ids;

    public List<String> groupIds;
}
