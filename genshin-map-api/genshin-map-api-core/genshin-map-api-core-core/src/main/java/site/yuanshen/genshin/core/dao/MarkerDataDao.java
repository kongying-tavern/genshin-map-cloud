package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.proto.MarkerDiffSnapshotVoOuterClass;
import site.yuanshen.data.proto.MarkerVoListOuterClass;
import site.yuanshen.data.proto.MarkerVoOuterClass;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.marker.marker.MarkerExtraVo;

import java.util.List;

public interface MarkerDataDao {
    MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo buildMarkerDiffSnapshotProto(MarkerVo markerVo);

    MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList buildMarkerDiffSnapshotListProto(List<MarkerVo> markerVoList);

    MarkerVoOuterClass.MarkerItemLinkVo buildMarkerItemLinkProto(MarkerItemLinkVo markerItemLinkVo);

    MarkerVoOuterClass.MarkerExtra buildMarkerExtraProto(MarkerExtraVo markerExtraVo);

    MarkerVoOuterClass.MarkerVo buildMarkerProto(MarkerVo markerVo);

    MarkerVoListOuterClass.MarkerVoList buildMarkerListProto(List<MarkerVo> markerVoList);
}
