package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.entity.MarkerLinkage;

import java.util.List;

/**
 * 点位关联的数据查询层
 *
 * @author Alex Fang
 */
public interface MarkerLinkageDao {

    List<MarkerLinkage> getRelatedLinkageList(List<Long> idList, boolean includeDeleted);

    boolean saveOrUpdateBatch(List<MarkerLinkage> markerLinkageList);
}
