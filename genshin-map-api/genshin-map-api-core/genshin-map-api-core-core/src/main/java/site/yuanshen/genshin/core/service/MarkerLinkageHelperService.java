package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.MarkerLinkageDataHelper;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;
import site.yuanshen.genshin.core.service.mbp.MarkerMBPService;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 点位关联服务辅助实现
 *
 * @author Alex Fang
 */
@Service
@RequestMapping
@RequiredArgsConstructor
public class MarkerLinkageHelperService {

    private final MarkerMBPService markerMBPService;
    private final MarkerLinkageMBPService markerLinkageMBPService;

    public void checkLinkList(List<MarkerLinkageVo> linkageVos) {
        for(MarkerLinkageVo linkageVo : linkageVos) {
            final Long fromId = linkageVo.getFromId();
            final Long toId = linkageVo.getToId();
            if(fromId == null || fromId.compareTo(0L) <= 0 || toId == null || toId.compareTo(0L) <= 0) {
                throw new GenshinApiException("无效的关联节点ID");
            } else if(fromId.compareTo(toId) == 0) {
                throw new GenshinApiException("不能将点位关联到自身");
            }
        }
        if(CollUtil.isEmpty(linkageVos)) {
            throw new GenshinApiException("关联数据不可为空");
        }
    }

    @Cacheable(value = "getMarkerLinkageList")
    public List<MarkerLinkageVo> getLinkageList(List<String> groupIds) {
        if(CollUtil.isEmpty(groupIds)) {
            return new ArrayList<>();
        }

        // 获取关联列表
        final List<MarkerLinkageVo> linkageList = markerLinkageMBPService.list(Wrappers.<MarkerLinkage>lambdaQuery().in(MarkerLinkage::getGroupId, groupIds)).parallelStream()
            .map(markerLinkage -> BeanUtils.copy(markerLinkage, MarkerLinkageVo.class)).collect(Collectors.toList());
        MarkerLinkageDataHelper.reverseLinkageIds(linkageList);
        return linkageList;
    }

    @Cacheable(value = "getMarkerLinkageGraph")
    public Map<String, GraphVo> getLinkageGraph(List<String> groupIds) {
        if(CollUtil.isEmpty(groupIds)) {
            return new HashMap<>();
        }

        final MarkerLinkageHelperService markerLinkageHelperService = (MarkerLinkageHelperService) SpringContextUtils.getBean("markerLinkageHelperService");
        // 获取关联绘图数据
        final List<MarkerLinkageVo> linkageList = markerLinkageHelperService.getLinkageList(groupIds);
        final Map<String, GraphVo> linkageGraph = MarkerLinkageDataHelper.buildLinkageGraph(linkageList);
        return linkageGraph;
    }


    @Cacheable(value = "getMarkerLinkagePathCoords")
    public Map<Long, Point2D.Double> getPathCoords(List<Long> markerIds) {
        if(CollUtil.isEmpty(markerIds)) {
            return new HashMap<>();
        }
        final List<Marker> markerList = markerMBPService.list(Wrappers.<Marker>lambdaQuery().in(Marker::getId, markerIds));
        final Map<Long, Point2D.Double> markerCoords = new ConcurrentHashMap<>();
        markerList.parallelStream()
                .forEach(marker -> {
                    final Long markerId = ObjectUtil.defaultIfNull(marker.getId(), 0L);
                    final String position = StrUtil.blankToDefault(marker.getPosition(), "");
                    final List<String> posChunks = StrUtil.split(position, ',', 2);
                    if(posChunks.size() >= 2) {
                        try {
                            final double x = NumberUtil.parseDouble(posChunks.get(0));
                            final double y = NumberUtil.parseDouble(posChunks.get(1));
                            final Point2D.Double coords = new Point2D.Double(x, y);
                            markerCoords.putIfAbsent(markerId, coords);
                        } catch (Exception e) {
                            // Ignore invalid coordinates
                        }
                    }
                });
        return markerCoords;
    }
}
