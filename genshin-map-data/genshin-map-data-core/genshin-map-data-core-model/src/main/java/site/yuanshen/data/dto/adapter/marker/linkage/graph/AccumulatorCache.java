package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a temporarily DTO adapter to hold accumulated data. These data should be reformed to VO for API usage.
 * The thread safe datasets in this DTO is due to upper-level parallel calls.
 */
@Getter
public class AccumulatorCache {
    public AccumulatorCache() {
        this.newId();
    }

    // 缓存定位数据
    private String cacheId = "";

    public void newId() {
        this.cacheId = IdUtil.fastSimpleUUID();
    }

    // 关联相关数据
    private ConcurrentHashSet<LinkRefDto> linkageSet = new ConcurrentHashSet<>();

    public boolean inLinkage(LinkRefDto linkRef, boolean useFrom, boolean useTo) {
        if(linkRef == null) {
            // Make this `true` to avoid further addition to linkage
            return true;
        }
        return this.inLinkage(linkRef.getFromId(), linkRef.getToId(), useFrom, useTo);
    }

    public boolean inLinkage(MarkerLinkageVo linkageVo, boolean useFrom, boolean useTo) {
        if(linkageVo == null) {
            // Make this `true` to avoid further addition to linkage
            return true;
        }
        return this.inLinkage(linkageVo.getFromId(), linkageVo.getToId(), useFrom, useTo);
    }

    public boolean inLinkage(Long srcFromId, Long srcToId, boolean useFrom, boolean useTo) {
        srcFromId = ObjectUtil.defaultIfNull(srcFromId, 0L);
        srcToId = ObjectUtil.defaultIfNull(srcToId, 0L);
        boolean validFromId = srcFromId.compareTo(0L) > 0;
        boolean validToId = srcToId.compareTo(0L) > 0;
        if(!validFromId && !validToId) {
            // Make this `true` to avoid further addition to linkage
            return true;
        }

        for(LinkRefDto linkRef : this.linkageSet) {
            final Long tarFromId = ObjectUtil.defaultIfNull(linkRef.getFromId(), 0L);
            final Long tarToId = ObjectUtil.defaultIfNull(linkRef.getToId(), 0L);
            if(useFrom && validFromId && tarFromId.equals(srcFromId)) return true;
            if(useFrom && validFromId && tarFromId.equals(srcToId)) return true;
            if(useTo && validToId && tarToId.equals(srcFromId)) return true;
            if(useTo && validToId && tarToId.equals(srcToId)) return true;
        }
        return false;
    }

    public void addLinkage(LinkRefDto linkRef) {
        if(linkRef == null) {
            return;
        }
        this.linkageSet.add(linkRef);
    }

    // 路径相关数据
    private ConcurrentHashMap<Long, List<PathEdgeVo>> pathMap = new ConcurrentHashMap<>();

    public void addPath(MarkerLinkageVo linkage) {
        if(linkage == null) {
            return;
        }
        final List<PathEdgeVo> pathList = CollUtil.defaultIfEmpty(linkage.getPath(), List.of());
        final Long linkageId = ObjectUtil.defaultIfNull(linkage.getId(), 0L);
        this.pathMap.putIfAbsent(linkageId, pathList);
    }
}
