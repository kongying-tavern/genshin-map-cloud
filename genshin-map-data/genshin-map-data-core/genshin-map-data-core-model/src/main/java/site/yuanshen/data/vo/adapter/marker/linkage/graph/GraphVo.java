package site.yuanshen.data.vo.adapter.marker.linkage.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "绘图数据前端封装")
public class GraphVo implements Serializable {
    @Schema(title = "点位关联关系")
    private Map<Long, List<String>> relations = new HashMap<>();

    @Schema(title = "点位关联关系引用映射")
    private Map<String, RelationVo> relRefs = new HashMap<>();

    @Schema(title = "路线组引用映射")
    private Map<Long, List<PathEdgeVo>> pathRefs = new HashMap<>();

}
