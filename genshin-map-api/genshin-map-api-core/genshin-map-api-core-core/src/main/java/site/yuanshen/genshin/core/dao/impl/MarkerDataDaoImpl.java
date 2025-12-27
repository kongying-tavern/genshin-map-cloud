package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.proto.MarkerDiffSnapshotVoOuterClass;
import site.yuanshen.data.proto.MarkerVoListOuterClass;
import site.yuanshen.data.proto.MarkerVoOuterClass;
import site.yuanshen.data.proto.SysUserSmallVoOuterClass;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.data.vo.adapter.marker.marker.MarkerExtraVo;
import site.yuanshen.genshin.core.dao.MarkerDataDao;
import site.yuanshen.genshin.core.dao.SysUserDataDao;
import site.yuanshen.genshin.core.service.UserAppenderService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkerDataDaoImpl implements MarkerDataDao {
    private final SysUserDataDao sysUserDataDao;

    @Override
    public MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo buildMarkerDiffSnapshotProto(MarkerVo markerVo) {
        if (markerVo == null) {
            return null;
        }

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo snapshotProto = MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo.newBuilder()
            .setVersion(markerVo.getVersion())
            .setId(markerVo.getId())
            .build();
        return snapshotProto;
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
        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList snapshotListProto = builder.build();

        return snapshotListProto;
    }

    @Override
    public MarkerVoOuterClass.MarkerItemLinkVo buildMarkerItemLinkProto(MarkerItemLinkVo markerItemLinkVo) {
        if (markerItemLinkVo == null) {
            return null;
        }

        final MarkerVoOuterClass.MarkerItemLinkVo itemLinkProto = MarkerVoOuterClass.MarkerItemLinkVo.newBuilder()
            .setItemId(markerItemLinkVo.getItemId())
            .setIconId(markerItemLinkVo.getIconId())
            .setCount(markerItemLinkVo.getCount())
            .build();
        return itemLinkProto;
    }

    @Override
    public MarkerVoOuterClass.MarkerExtra buildMarkerExtraProto(MarkerExtraVo markerExtraVo) {
        if (markerExtraVo == null) {
            return MarkerVoOuterClass.MarkerExtra.getDefaultInstance();
        }

        final MarkerVoOuterClass.MarkerExtra.Builder builder = MarkerVoOuterClass.MarkerExtra.newBuilder();
        // Underground
        if (markerExtraVo.getUnderground() != null) {
            final MarkerVoOuterClass.MarkerExtraUnderground.Builder undergroundProtoBuilder = MarkerVoOuterClass.MarkerExtraUnderground.newBuilder();
            undergroundProtoBuilder
                .setIsUnderground(markerExtraVo.getUnderground().getIsUnderground())
                .setIsGlobal(markerExtraVo.getUnderground().getIsGlobal())
                .addAllRegionLevels(CollUtil.emptyIfNull(markerExtraVo.getUnderground().getRegionLevels()));
            final MarkerVoOuterClass.MarkerExtraUnderground undergroundProto = undergroundProtoBuilder.build();
            builder.setUnderground(undergroundProto);
        }

        // IconOverride
        if (markerExtraVo.getIconOverride() != null) {
            final MarkerVoOuterClass.MarkerExtraIconOverride.Builder iconOverrideProtoBuilder = MarkerVoOuterClass.MarkerExtraIconOverride.newBuilder();
            iconOverrideProtoBuilder
                .setId(markerExtraVo.getIconOverride().getId())
                .setMinZoom(markerExtraVo.getIconOverride().getMinZoom().floatValue())
                .setMaxZoom(markerExtraVo.getIconOverride().getMaxZoom().floatValue());
            final MarkerVoOuterClass.MarkerExtraIconOverride iconOverrideProto = iconOverrideProtoBuilder.build();
            builder.setIconOverride(iconOverrideProto);
        }

        // V1_6_Island
        if (markerExtraVo.getV1_6_Island() != null) {
            builder.addAllV16Island(markerExtraVo.getV1_6_Island());
        }

        // V2_8_Island
        if (markerExtraVo.getV2_8_Island() != null) {
            final MarkerVoOuterClass.MarkerExtra2_8_Island.Builder v28IslandProtoBuilder = MarkerVoOuterClass.MarkerExtra2_8_Island.newBuilder();
            v28IslandProtoBuilder
                .setIslandName(markerExtraVo.getV2_8_Island().getIslandName())
                .addAllIslandState(CollUtil.emptyIfNull(markerExtraVo.getV2_8_Island().getIslandState()));
            final MarkerVoOuterClass.MarkerExtra2_8_Island v28IslandProto = v28IslandProtoBuilder.build();
            builder.setV28Island(v28IslandProto);
        }

        final MarkerVoOuterClass.MarkerExtra markerExtraProto = builder.build();
        return markerExtraProto;
    }

    @Override
    public MarkerVoOuterClass.MarkerVo buildMarkerProto(MarkerVo markerVo) {
        if (markerVo == null) {
            return null;
        }

        final MarkerVoOuterClass.MarkerVo.Builder builder = MarkerVoOuterClass.MarkerVo.newBuilder();
        builder
            .setVersion(markerVo.getVersion())
            .setId(markerVo.getId())
            .setCreatorId(markerVo.getCreatorId())
            .setCreateTime(TimeUtils.toProtoTimestamp(markerVo.getCreateTime()))
            .setUpdaterId(markerVo.getUpdaterId())
            .setUpdateTime(TimeUtils.toProtoTimestamp(markerVo.getUpdateTime()))
            .setMarkerTitle(markerVo.getMarkerTitle())
            .setPosition(markerVo.getPosition())
            .setContent(markerVo.getContent())
            .setPicture(markerVo.getPicture())
            .setVideoPath(markerVo.getVideoPath())
            .setRefreshTime(markerVo.getRefreshTime())
            .setHiddenFlag(markerVo.getHiddenFlag())
            .addAllItemList(
                CollUtil.emptyIfNull(markerVo.getItemList())
                    .stream()
                    .map(this::buildMarkerItemLinkProto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            )
            .setMarkerCreatorId(markerVo.getMarkerCreatorId())
            .setPictureCreatorId(markerVo.getPictureCreatorId())
            .setMarkerStamp(markerVo.getMarkerStamp())
            .setExtra(this.buildMarkerExtraProto(markerVo.getExtra()))
            .setLinkageId(markerVo.getLinkageId());
        final MarkerVoOuterClass.MarkerVo markerVoProto = builder.build();

        return markerVoProto;
    }

    @Override
    public MarkerVoListOuterClass.MarkerVoList buildMarkerListProto(List<MarkerVo> markerVoList) {
        if (CollUtil.isEmpty(markerVoList)) {
            markerVoList = List.of();
        }

        final MarkerVoListOuterClass.MarkerVoList.Builder builder = MarkerVoListOuterClass.MarkerVoList.newBuilder();

        // Markers
        builder.addAllMarkers(
            markerVoList
                .stream()
                .map(this::buildMarkerProto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        // Users
        Map<Long, SysUserSmallVo> userMapFromCreator = UserAppenderService.getUserMap(markerVoList, MarkerVo::getCreatorId);
        Map<Long, SysUserSmallVo> userMapFromUpdater = UserAppenderService.getUserMap(markerVoList, MarkerVo::getUpdaterId);
        Map<Long, SysUserSmallVo> userMap = Stream
            .concat(
                userMapFromCreator.entrySet().stream(),
                userMapFromUpdater.entrySet().stream()
            )
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (o, n) -> n
            ));
        for (Map.Entry<Long, SysUserSmallVo> entry : userMap.entrySet()) {
            final Long userId = entry.getKey();
            final SysUserSmallVo user = entry.getValue();
            if (userId == null || user == null) {
                continue;
            }
            SysUserSmallVoOuterClass.SysUserSmallVo userSmallProto = this.sysUserDataDao.buildSysUserSmallProto(user);
            if (userSmallProto == null) {
                return null;
            }
            builder.putUsers(userId, userSmallProto);
        }

        final MarkerVoListOuterClass.MarkerVoList markerVoListProto = builder.build();
        return markerVoListProto;
    }
}
