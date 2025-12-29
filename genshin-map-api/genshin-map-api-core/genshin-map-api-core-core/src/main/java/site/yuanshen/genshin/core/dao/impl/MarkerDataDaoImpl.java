package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo.Builder snapshotBuilder = MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo.newBuilder();

        snapshotBuilder.setVersion(markerVo.getVersion() == null ? 0L : markerVo.getVersion());
        snapshotBuilder.setId(markerVo.getId() == null ? 0L : markerVo.getId());
        if (StrUtil.isNotBlank(markerVo.getLinkageId())) {
            snapshotBuilder.setLinkageId(StrUtil.nullToEmpty(markerVo.getLinkageId()));
        }

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVo snapshotProto = snapshotBuilder.build();

        return snapshotProto;
    }

    @Override
    public MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList buildMarkerDiffSnapshotListProto(List<MarkerVo> markerVoList) {
        if (CollUtil.isEmpty(markerVoList)) {
            markerVoList = List.of();
        }

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList.Builder builder = MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList.newBuilder();
        builder.addAllSnapshots(
            markerVoList.stream()
                .map(this::buildMarkerDiffSnapshotProto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList snapshotListProto = builder.build();

        return snapshotListProto;
    }

    @Override
    public MarkerVoOuterClass.MarkerItemLinkVo buildMarkerItemLinkProto(MarkerItemLinkVo markerItemLinkVo) {
        if (markerItemLinkVo == null) {
            return null;
        }

        final MarkerVoOuterClass.MarkerItemLinkVo.Builder itemLinkBuilder = MarkerVoOuterClass.MarkerItemLinkVo.newBuilder();

        itemLinkBuilder.setItemId(markerItemLinkVo.getItemId() == null ? 0L : markerItemLinkVo.getItemId());
        itemLinkBuilder.setIconId(markerItemLinkVo.getIconId() == null ? 0L : markerItemLinkVo.getIconId());
        itemLinkBuilder.setCount(markerItemLinkVo.getCount() == null ? 1 : markerItemLinkVo.getCount());

        final MarkerVoOuterClass.MarkerItemLinkVo itemLinkProto = itemLinkBuilder.build();

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
            final MarkerVoOuterClass.MarkerExtraUnderground.Builder undergroundBuilder = MarkerVoOuterClass.MarkerExtraUnderground.newBuilder();
            undergroundBuilder.setIsUnderground(
                markerExtraVo.getUnderground().getIsUnderground() != null &&
                    markerExtraVo.getUnderground().getIsUnderground()
            );
            if (markerExtraVo.getUnderground().getIsGlobal() != null) {
                undergroundBuilder.setIsGlobal(markerExtraVo.getUnderground().getIsGlobal());
            }
            undergroundBuilder.addAllRegionLevels(
                CollUtil.emptyIfNull(markerExtraVo.getUnderground().getRegionLevels())
            );

            final MarkerVoOuterClass.MarkerExtraUnderground undergroundProto = undergroundBuilder.build();

            builder.setUnderground(undergroundProto);
        }

        // IconOverride
        if (markerExtraVo.getIconOverride() != null) {
            final MarkerVoOuterClass.MarkerExtraIconOverride.Builder iconOverrideProtoBuilder = MarkerVoOuterClass.MarkerExtraIconOverride.newBuilder();

            iconOverrideProtoBuilder.setId(
                markerExtraVo.getIconOverride().getId() == null ?
                    0L :
                    markerExtraVo.getIconOverride().getId()
            );
            iconOverrideProtoBuilder.setMinZoom(
                markerExtraVo.getIconOverride().getMinZoom() == null ?
                    0.0f :
                    markerExtraVo.getIconOverride().getMinZoom().floatValue()
            );
            iconOverrideProtoBuilder.setMaxZoom(
                markerExtraVo.getIconOverride().getMaxZoom() == null ?
                    0.0f :
                    markerExtraVo.getIconOverride().getMaxZoom().floatValue()
            );

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

            v28IslandProtoBuilder.setIslandName(StrUtil.nullToEmpty(markerExtraVo.getV2_8_Island().getIslandName()));
            v28IslandProtoBuilder.addAllIslandState(
                CollUtil.emptyIfNull(markerExtraVo.getV2_8_Island().getIslandState())
            );

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
        builder.setVersion(markerVo.getVersion() == null ? 0L : markerVo.getVersion());
        builder.setId(markerVo.getId() == null ? 0L : markerVo.getId());
        builder.setCreatorId(markerVo.getCreatorId() == null ? 0L : markerVo.getCreatorId());
        if (markerVo.getCreateTime() != null) {
            builder.setCreateTime(markerVo.getCreateTime().getTime());
        }
        builder.setUpdaterId(markerVo.getUpdaterId() == null ? 0L : markerVo.getUpdaterId());
        if (markerVo.getUpdateTime() != null) {
            builder.setUpdateTime(markerVo.getUpdateTime().getTime());
        }
        builder.setMarkerTitle(StrUtil.nullToEmpty(markerVo.getMarkerTitle()));
        builder.setPosition(StrUtil.blankToDefault(markerVo.getPosition(), "0,0"));
        builder.setContent(StrUtil.nullToEmpty(markerVo.getContent()));
        builder.setPicture(StrUtil.nullToEmpty(markerVo.getPicture()));
        builder.setVideoPath(StrUtil.nullToEmpty(markerVo.getVideoPath()));
        builder.setRefreshTime(markerVo.getRefreshTime() == null ? 0L : markerVo.getRefreshTime());
        builder.setHiddenFlag(markerVo.getHiddenFlag() == null ? 0 : markerVo.getHiddenFlag());
        builder.addAllItemList(
            CollUtil.emptyIfNull(markerVo.getItemList())
                .stream()
                .map(this::buildMarkerItemLinkProto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );
        if (markerVo.getMarkerCreatorId() != null) {
            builder.setMarkerCreatorId(markerVo.getMarkerCreatorId());
        }
        if (markerVo.getPictureCreatorId() != null) {
            builder.setPictureCreatorId(markerVo.getPictureCreatorId());
        }
        builder.setMarkerStamp(StrUtil.nullToEmpty(markerVo.getMarkerStamp()));
        builder.setExtra(this.buildMarkerExtraProto(markerVo.getExtra()));
        builder.setLinkageId(StrUtil.nullToEmpty(markerVo.getLinkageId()));

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
                continue;
            }
            builder.putUsers(userId, userSmallProto);
        }

        final MarkerVoListOuterClass.MarkerVoList markerVoListProto = builder.build();

        return markerVoListProto;
    }
}
