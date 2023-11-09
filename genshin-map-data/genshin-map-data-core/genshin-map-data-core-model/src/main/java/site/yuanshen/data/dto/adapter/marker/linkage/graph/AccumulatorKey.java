package site.yuanshen.data.dto.adapter.marker.linkage.graph;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.data.enums.marker.linkage.LinkActionEnum;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class AccumulatorKey {

    private String groupId;

    private LinkActionEnum linkAction;
}
