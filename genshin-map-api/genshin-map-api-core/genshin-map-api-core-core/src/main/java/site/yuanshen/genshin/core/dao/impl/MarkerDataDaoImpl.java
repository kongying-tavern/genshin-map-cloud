package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.proto.MarkerDiffSnapshotVoOuterClass;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.genshin.core.dao.MarkerDataDao;

import java.util.List;

@Slf4j
@Service
public class MarkerDataDaoImpl implements MarkerDataDao {
    @Override
    public MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo buildMarkerDiffSnapshotProto(MarkerVo markerVo) {
        if (markerVo == null) {
            return null;
        }

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo snapshot = MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo.newBuilder()
            .setVersion(markerVo.getVersion())
            .setId(markerVo.getId())
            .build();
        return snapshot;
    }

    @Override
    public MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList buildMarkerDiffSnapshotListProto(List<MarkerVo> markerVoList) {
        if (CollUtil.isEmpty(markerVoList)) {
            markerVoList = List.of();
        }

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList.Builder builder = MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList.newBuilder();
        markerVoList.forEach(markerVo -> {
            MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo snapshot = this.buildMarkerDiffSnapshotProto(markerVo);
            if (snapshot == null) {
                return;
            }
            builder.addSnapshots(snapshot);
        });
        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList snapshotList = builder.build();

        return snapshotList;
    }
}
