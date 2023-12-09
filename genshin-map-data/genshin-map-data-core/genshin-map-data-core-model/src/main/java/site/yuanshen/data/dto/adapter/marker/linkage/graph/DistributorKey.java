package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class DistributorKey {

    private String groupId;

    private String linkGroupId;

    private String linkAction;

    private Long markerId;

}
