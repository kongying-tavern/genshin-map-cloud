package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.genshin.core.dao.MarkerDao;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkerDaoImpl implements MarkerDao {

    private final CacheManager cacheManager;
    private final MarkerMapper markerMapper;
    private final MarkerExtraMapper markerExtraMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final ItemMapper itemMapper;


    @Override
    @Cacheable(value = "getMarkerCount")
    public Long getMarkerCount(Boolean isTestUser) {
        return markerMapper.selectCount(Wrappers.<Marker>lambdaQuery().ne(!isTestUser, Marker::getHiddenFlag, 2));
    }

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param isTestUser    是否是测试服打点用户
     * @return 点位完整信息的前端封装的分页记录
     */
    @Override
    @Cacheable(value = "listMarkerPage")
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, Boolean isTestUser) {
        Page<Marker> markerPage = markerMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<Marker>lambdaQuery().ne(!isTestUser, Marker::getHiddenFlag, 2));
        List<Long> markerIdList = markerPage.getRecords().stream()
                .map(Marker::getId).collect(Collectors.toList());
        Map<Long, MarkerExtra> extraMap = markerExtraMapper.selectList(Wrappers.<MarkerExtra>lambdaQuery()
                        .in(MarkerExtra::getMarkerId, markerIdList))
                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra));
        Map<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        List<MarkerItemLink> markerItemLinks = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().in(MarkerItemLink::getMarkerId, markerIdList));
        markerItemLinks.parallelStream().forEach(markerItemLink ->
                itemLinkMap.compute(markerItemLink.getMarkerId(),
                        (markerId, linkList) -> {
                            if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                            linkList.add(markerItemLink);
                            return linkList;
                        }));
        //获取item_id,得到item合集
        Map<Long, Item> itemMap = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(Item::getId, markerItemLinks.stream().map(MarkerItemLink::getItemId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Item::getId, Item -> Item));


        return new PageListVo<MarkerVo>()
                .setRecord(markerPage.getRecords().parallelStream()
                        .map(marker -> new MarkerDto(marker, extraMap.get(marker.getId()), itemLinkMap.get(marker.getId()), itemMap).getVo())
                        .collect(Collectors.toList()))
                .setTotal(markerPage.getTotal())
                .setSize(markerPage.getSize());
    }

    /**
     * 通过bz2返回点位分页
     *
     * @param isTestUser 是否是测试打点用户
     * @param index      下标（从1开始）
     * @return 压缩后的字节数组
     */
    @Override
    @Cacheable(value = "listPageMarkerByBz2", key = "#isTestUser+'#'+#index")
    public byte[] listPageMarkerByBz2(Boolean isTestUser, Long index) {
        try {
            return CompressUtils.compress(JSON.toJSONString(
                            listMarkerPage(new PageSearchDto(index, 3000L), isTestUser))
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("创建压缩失败" + e);
        }
    }

    /**
     * 返回点位分页bz2的md5数组
     *
     * @param isTestUser 是否是测试打点用户
     * @return 分页字节数组的md5
     */
    @Override
    @Cacheable(value = "listMarkerBz2MD5")
    public List<String> listMarkerBz2MD5(Boolean isTestUser) {
        Cache markerBz2Cache = cacheManager.getCache("listPageMarkerByBz2");

        long totalPages = (getMarkerCount(isTestUser) + 3000 - 1) / 3000;
        List<Long> indexList = new ArrayList<>();
        for (long i = 1; i <= totalPages; i++) {
            indexList.add(i);
        }
        List<String> markerBz2MD5List;
        if (markerBz2Cache != null) {
            try {
                markerBz2MD5List = indexList.parallelStream().map(i -> {
                    Cache.ValueWrapper wrapper = markerBz2Cache.get("false#" + i);
                    byte[] markerBz2;
                    if (wrapper == null || wrapper.get() == null || wrapper.get() instanceof byte[]) {
                        log.warn("rebuild the marker page bz2:{}", i);
                        markerBz2 = listPageMarkerByBz2(false, i);
                    } else {
                        markerBz2 = (byte[]) wrapper.get();
                    }
                    return DigestUtils.md5DigestAsHex(markerBz2);
                }).collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("打包MD5缓存创建失败");
            }
        } else {
            markerBz2MD5List = indexList.parallelStream().map(i -> DigestUtils.md5DigestAsHex(listPageMarkerByBz2(false, i))).collect(Collectors.toList());
        }
        return markerBz2MD5List;
    }
}
