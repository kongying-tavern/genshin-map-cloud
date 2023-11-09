package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.data.enums.marker.linkage.IdTypeEnum;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.LinkRefVo;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class LinkRefDto {

    private Long fromId;

    private Long toId;

    private Long pathRefId;

    public LinkRefVo toVo(IdTypeEnum idType) {
        switch (idType) {
            case FROM:
                return new LinkRefVo()
                        .withMarkerId(this.fromId)
                        .withPathRefId(this.pathRefId);
            case TO:
                return new LinkRefVo()
                        .withMarkerId(this.toId)
                        .withPathRefId(this.pathRefId);
            default:
                return null;
        }
    }
}
