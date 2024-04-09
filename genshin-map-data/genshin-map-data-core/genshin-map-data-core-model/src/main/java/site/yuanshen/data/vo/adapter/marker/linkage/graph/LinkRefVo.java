package site.yuanshen.data.vo.adapter.marker.linkage.graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "关联引用前端封装")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LinkRefVo implements Serializable {
    @Schema(title = "点位ID")
    private Long markerId;

    @Schema(title = "起始点位ID")
    private Long srcId;

    @Schema(title = "结束点位ID")
    private Long tarId;

    @Schema(title = "路线组引用ID")
    private Long pathRefId;
}
