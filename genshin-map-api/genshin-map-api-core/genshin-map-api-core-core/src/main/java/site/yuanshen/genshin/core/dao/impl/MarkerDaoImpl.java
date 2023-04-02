package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerExtra;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.mapper.MarkerExtraMapper;
import site.yuanshen.data.mapper.MarkerItemLinkMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@Service
public class MarkerDaoImpl implements MarkerDao {

    private final MarkerMapper markerMapper;
    private final MarkerExtraMapper markerExtraMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final ItemMapper itemMapper;
    private final CacheManager cacheManager;
    private final CacheManager neverRefreshCacheManager;

    @Autowired
    public MarkerDaoImpl(MarkerMapper markerMapper,
                         MarkerExtraMapper markerExtraMapper,
                         MarkerItemLinkMapper markerItemLinkMapper,
                         ItemMapper itemMapper,
                         CacheManager cacheManager,
                         @Qualifier("neverRefreshCacheManager")
                         CacheManager neverRefreshCacheManager) {
        this.markerMapper = markerMapper;
        this.markerExtraMapper = markerExtraMapper;
        this.markerItemLinkMapper = markerItemLinkMapper;
        this.itemMapper = itemMapper;
        this.cacheManager = cacheManager;
        this.neverRefreshCacheManager = neverRefreshCacheManager;
    }

    @Override
    @Cacheable(value = "getMarkerCount")
    public Long getMarkerCount(List<Integer> hiddenFlagList) {
        return markerMapper.selectCount(Wrappers.<Marker>lambdaQuery().in(!hiddenFlagList.isEmpty(), Marker::getHiddenFlag, hiddenFlagList));
    }

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto  分页查询数据封装
     * @param hiddenFlagList hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    @Override
    @Cacheable(value = "listMarkerPage")
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList) {
        Page<Marker> markerPage = markerMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<Marker>lambdaQuery().in(!hiddenFlagList.isEmpty(), Marker::getHiddenFlag, hiddenFlagList));
        List<Long> markerIdList = markerPage.getRecords().stream()
                .map(Marker::getId).collect(Collectors.toList());

        Map<Long, MarkerExtra> extraMap = new HashMap<>();
        ConcurrentHashMap<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        Map<Long, Item> itemMap = new HashMap<>();
        getAllRelateInfoById(markerIdList, extraMap, itemLinkMap, itemMap);

        return new PageListVo<MarkerVo>()
                .setRecord(markerPage.getRecords().parallelStream()
                        .map(marker -> new MarkerDto(marker, extraMap.get(marker.getId()), itemLinkMap.get(marker.getId()), itemMap).getVo())
                        .collect(Collectors.toList()))
                .setTotal(markerPage.getTotal())
                .setSize(markerPage.getSize());
    }

    public void getAllRelateInfoById(List<Long> markerIdList, Map<Long, MarkerExtra> extraMap, ConcurrentHashMap<Long, List<MarkerItemLink>> itemLinkMap, Map<Long, Item> itemMap) {
        String s = markerIdList.toString().replace('[', '{').replace(']', '}');
//        extraMap.putAll(markerExtraMapper.selectList(Wrappers.<MarkerExtra>lambdaQuery()
//                        .apply("marker_id = any({0}::bigint[])", s))
//                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra)));
        extraMap.putAll(markerExtraMapper.selectWithLargeCustomIn("marker_id", PgsqlUtils.unnestStr(markerIdList), Wrappers.<MarkerExtra>lambdaQuery())
                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra)));


//        List<Long> itemIdList = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
//                        .apply("marker_id = any({0}::bigint[])", s))
        List<Long> itemIdList = markerItemLinkMapper.selectWithLargeCustomIn("marker_id", PgsqlUtils.unnestStr(markerIdList), Wrappers.<MarkerItemLink>lambdaQuery())
                .parallelStream().map(markerItemLink -> {
                    itemLinkMap.compute(markerItemLink.getMarkerId(),
                            (markerId, linkList) -> {
                                if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                                linkList.add(markerItemLink);
                                return linkList;
                            });
                    return markerItemLink.getItemId();
                })
                .distinct().collect(Collectors.toList());
        //获取item_id,得到item合集
        itemMap.putAll(itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .apply("id = any({0}::bigint[])", itemIdList.toString().replace('[', '{').replace(']', '}')))
                .stream().collect(Collectors.toMap(Item::getId, Item -> Item)));
    }

    /**
     * 通过bz2返回点位分页
     *
     * @param index 下标（从1开始）
     * @return 压缩后的字节数组
     */
    @Override
    @Cacheable(value = "listPageMarkerByBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] listPageMarkerByBz2(Integer index) {
        Cache bz2Cache = cacheManager.getCache("listPageMarkerByBz2");
        Cache neverBz2Cache = neverRefreshCacheManager.getCache("listPageMarkerByBz2");
        throw new RuntimeException("缓存未创建");
    }

    /**
     * 刷新bz2返回点位分页
     *
     * @return 刷新后的各个分页
     */
    public List<byte[]> refreshPageMarkerByBz2() {
        try {
            List<MarkerVo> markerList = getAllMarkerVo();
            markerList.sort(Comparator.comparingLong(MarkerVo::getId));
            Long lastId = markerList.get(markerList.size() - 1).getId();
            int totalPages = (int) ((lastId + 3000 - 1) / 3000);
            List<byte[]> result = new ArrayList<>();
            Cache bz2Cache = neverRefreshCacheManager.getCache("listPageMarkerByBz2");
            if (bz2Cache == null) throw new RuntimeException("缓存未初始化");
            for (int i = 0; i < totalPages; i++) {
                int finalI = i;
                byte[] page = JSON.toJSONString(
                                markerList.parallelStream()
                                        .filter(markerVo -> markerVo.getId() >= (finalI * 3000L) && markerVo.getId() < ((finalI + 1) * 3000L))
                                        .sorted(Comparator.comparingLong(MarkerVo::getId)).collect(Collectors.toList()))
                        .getBytes(StandardCharsets.UTF_8);
                byte[] compress = CompressUtils.compress(page);
                result.add(compress);
                bz2Cache.put(i, compress);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("创建压缩失败", e);
        }
    }

    private List<MarkerVo> getAllMarkerVo() {
        List<Marker> markerList = markerMapper.selectList(Wrappers.<Marker>lambdaQuery().eq(Marker::getHiddenFlag, 0));

        Map<Long, MarkerExtra> extraMap = (markerExtraMapper.selectList(Wrappers.lambdaQuery())
                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra)));

        //获取item_id,得到item合集
        Map<Long, Item> itemMap = itemMapper.selectList(Wrappers.<Item>lambdaQuery().eq(Item::getHiddenFlag, 0))
                .stream().collect(Collectors.toMap(Item::getId, Item -> Item));

        Set<Long> hideItemSet = new HashSet<>();

        ConcurrentHashMap<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        markerItemLinkMapper.selectList(Wrappers.lambdaQuery())
                .parallelStream().forEach(markerItemLink -> {
                            if (!itemMap.containsKey(markerItemLink.getItemId())) {
                                hideItemSet.add(markerItemLink.getItemId());
                                return;
                            }
                            itemLinkMap.compute(markerItemLink.getMarkerId(),
                                    (markerId, linkList) -> {
                                        if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                                        linkList.add(markerItemLink);
                                        return linkList;
                                    }
                            );
                        }
                );

        log.info("拥有点位的隐藏物品:{}", hideItemSet);

        return markerList.parallelStream()
                .map(marker -> new MarkerDto(marker, extraMap.get(marker.getId()), itemLinkMap.get(marker.getId()), itemMap).getVo())
                .collect(Collectors.toList());
    }

}
