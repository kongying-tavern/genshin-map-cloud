package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.vo.MarkerLinkageVo;

import java.util.List;

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
     * 所有的点位关联信息
     */
    List<MarkerLinkageVo> listAllMarkerLinkage();

    /**
     * 所有的点位关联信息的Bz2压缩
     */
    byte[] listAllMarkerLinkageBz2();

    /**
     * 刷新点位关联压缩缓存并返回压缩文档
     */
    byte[] refreshAllMarkerLinkageListBz2();
}
