package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.RelationVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphDto {

    // 关系相关
    private Map<Long, List<String>> relations = new HashMap<>();

    private Map<String, RelationVo> relRefs = new HashMap<>();

    public void addRel(Long id, String relId, RelationDto relation) {
        id = ObjectUtil.defaultIfNull(id, 0L);
        relId = StrUtil.blankToDefault(relId, "");
        if(id.compareTo(0L) <= 0) {
            return;
        } else if(StrUtil.isEmpty(relId)) {
            return;
        } else if(relation == null) {
            return;
        }

        this.relations.putIfAbsent(id, new ArrayList<>());
        List<String> relRefList = this.relations.get(id);
        relRefList.add(relId);
        relRefs.putIfAbsent(relId, relation.toVo());
    }

    // 路线相关
    private Map<Long, List<PathEdgeVo>> pathRefs = new HashMap<>();

    public void addPaths(Map<Long, List<PathEdgeVo>> pathMap) {
        if(CollUtil.isEmpty(pathMap)) {
            return;
        }
        this.pathRefs.putAll(pathMap);
    }

    public GraphVo toVo() {
        return new GraphVo()
                .withRelations(this.relations)
                .withRelRefs(this.relRefs)
                .withPathRefs(this.pathRefs);
    }
}
