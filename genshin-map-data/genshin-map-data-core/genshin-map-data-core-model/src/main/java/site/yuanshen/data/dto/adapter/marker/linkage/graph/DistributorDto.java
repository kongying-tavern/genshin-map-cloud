package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.Setter;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DistributorDto {
    // 关联组相关
    @Setter
    private String relationId = "";
    @Setter
    private RelationDto relation;

    // 路线引用相关
    private Map<Long, List<PathEdgeVo>> pathRefs = new HashMap<>();

    public void addPaths(Map<Long, List<PathEdgeVo>> pathMap) {
        if(CollUtil.isEmpty(pathMap)) {
            return;
        }
        this.pathRefs.putAll(pathMap);
    }
}
