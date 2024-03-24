package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 返回点位分页bz2的md5数组
     *
     * @return 分页字节数组的md5
     */

    @Cacheable(value = "listMarkerBz2MD5", cacheManager = "neverRefreshCacheManager")
    public List<String> listMarkerBz2MD5() {
        return new ArrayList<>();
    }

    @CachePut(value = "listMarkerBz2MD5", cacheManager = "neverRefreshCacheManager")
    public Map<String, String> refreshMarkerBz2MD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        Map<String, byte[]> bz2Map = markerDao.refreshPageMarkerByBz2();
        for(Map.Entry<String, byte[]> bz2Entry : bz2Map.entrySet()) {
            result.put(bz2Entry.getKey(), DigestUtils.md5DigestAsHex(bz2Entry.getValue()));
        }
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
