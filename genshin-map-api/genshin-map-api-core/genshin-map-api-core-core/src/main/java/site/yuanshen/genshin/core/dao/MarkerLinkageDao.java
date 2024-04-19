package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;

import java.util.List;
import java.util.Map;

/**
 * 点位关联的数据查询层
 *
 * @author Alex Fang
 */
public interface MarkerLinkageDao {

    /**
     * 获取相关的点位关联列表
     */
    List<MarkerLinkage> getRelatedLinkageList(List<Long> idList, boolean includeDeleted);

    /**
     * 删除相关的点位关联数据
     */
    boolean removeRelatedLinkageList(List<Long> idList, boolean includeDeleted);

    /**
     * 保存点位关联
     */
    boolean saveOrUpdateBatch(List<MarkerLinkage> markerLinkageList);

    /**
     * 所有的点位关联元数据
     */
    List<MarkerLinkageVo> getAllMarkerLinkage();

    /**
     * 所有的点位关联列表
     */
    Map<String, List<MarkerLinkageVo>> listAllMarkerLinkage();

    /**
     * 所有的点位关联有向图
     */
    Map<String, GraphVo> graphAllMarkerLinkage();

    /**
     * 所有的点位关联列表的压缩
     */
    byte[] listAllMarkerLinkageBinary();

    /**
     * 刷新点位关联列表压缩缓存并返回压缩文档
     */
    byte[] refreshAllMarkerLinkageListBinary();

    /**
     * 所有的点位关联有向图的压缩
     */
    byte[] graphAllMarkerLinkageBinary();

    /**
     * 刷新点位关联有向图压缩缓存并返回压缩文档
     */
    byte[] refreshAllMarkerLinkageGraphBinary();
}
