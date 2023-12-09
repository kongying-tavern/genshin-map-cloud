package site.yuanshen.data.vo.adapter.marker.linkage.graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(title = "关联组数据前端封装")
public class RelationVo implements Serializable {
    @Schema(title = "关联组类型")
    private String type = "";

    @Schema(title = "触发关联数据")
    private Set<LinkRefVo> triggers;

    @Schema(title = "目标关联数据")
    private Set<LinkRefVo> targets;

    @Schema(title = "分组关联数据")
    private Set<LinkRefVo> group;
}
