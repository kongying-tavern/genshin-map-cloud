package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.mapper.AreaMapper;
import site.yuanshen.data.proto.MarkerDiffSnapshotVoOuterClass;
import site.yuanshen.data.proto.MarkerVoListOuterClass;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.dao.MarkerDataDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点位压缩档案服务层实现
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerDocService {

    private final MarkerDao markerDao;
    private final MarkerDataDao markerDataDao;
    private final AreaMapper areaMapper;

    public List<String> listMarkerBinaryMD5() {
        return List.of();
    }

    public Map<String, String> refreshMarkerBinaryMD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new LinkedHashMap<>();
        markerDao.refreshMarkerBinaryList();
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }

    public byte[] getMarkerDiffSnapshot(List<Integer> hiddenFlagList) {
        final MarkerService markerService = (MarkerService) SpringContextUtils.getBean("markerService");
        final MarkerSearchVo markerSearchVo = new MarkerSearchVo();
        final List<Long> areaIds = areaMapper
            .selectList(Wrappers.lambdaQuery())
            .stream()
            .map(Area::getId)
            .collect(Collectors.toList());
        markerSearchVo.setAreaIdList(areaIds);
        final List<MarkerVo> markerList = markerService.searchMarker(markerSearchVo, hiddenFlagList);
        final MarkerDiffSnapshotVoOuterClass.MarkerDiffSnapshotVoList snapshotListProto = markerDataDao.buildMarkerDiffSnapshotListProto(markerList);

        return snapshotListProto.toByteArray();
    }

    public byte[] getMarkerList(List<Integer> hiddenFlagList) {
        final MarkerService markerService = (MarkerService) SpringContextUtils.getBean("markerService");
        final MarkerSearchVo markerSearchVo = new MarkerSearchVo();
        final List<Long> areaIds = areaMapper
            .selectList(Wrappers.lambdaQuery())
            .stream()
            .map(Area::getId)
            .collect(Collectors.toList());
        markerSearchVo.setAreaIdList(areaIds);
        final List<MarkerVo> markerList = markerService.searchMarker(markerSearchVo, hiddenFlagList);
        final MarkerVoListOuterClass.MarkerVoList markerListProto = markerDataDao.buildMarkerListProto(markerList);

        return markerListProto.toByteArray();
    }
}
