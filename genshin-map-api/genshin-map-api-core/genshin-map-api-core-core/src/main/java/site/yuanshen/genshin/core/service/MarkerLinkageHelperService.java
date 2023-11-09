package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.MarkerLinkageDataHelper;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final MarkerLinkageMBPService markerLinkageMBPService;

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

        // 获取关联绘图数据
        final MarkerLinkageHelperService helper = (MarkerLinkageHelperService) SpringContextUtils.getBean("markerLinkageHelperService");
        final List<MarkerLinkageVo> linkageList = helper.getLinkageList(groupIds);
        final Map<String, GraphVo> linkageGraph = MarkerLinkageDataHelper.buildLinkageGraph(linkageList);
        return linkageGraph;
    }

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
}
