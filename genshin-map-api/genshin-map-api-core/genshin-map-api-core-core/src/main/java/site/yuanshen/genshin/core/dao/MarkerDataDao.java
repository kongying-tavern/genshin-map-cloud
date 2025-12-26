package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.proto.MarkerDiffSnapshotVoOuterClass;
import site.yuanshen.data.vo.MarkerVo;

import java.util.List;

public interface MarkerDataDao {
    MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList getMarkerDiffSnapshotVoList(List<MarkerVo> markerList);
}
