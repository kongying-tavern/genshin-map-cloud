package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.util.LinkedHashMap;
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
    @Cacheable(value = "listMarkerBz2MD5", key = "''", cacheManager = "neverRefreshCacheManager")
    public Map<String, String> listAllMarkerBz2MD5() {
        return new LinkedHashMap<>();
    }

    @CachePut(value = "listMarkerBz2MD5", key = "''", cacheManager = "neverRefreshCacheManager")
    public Map<String, String> refreshMarkerBz2MD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, byte[]> bz2Map = markerDao.refreshPageMarkerByBz2();
        for(Map.Entry<String, byte[]> bz2Entry : bz2Map.entrySet()) {
            result.put(bz2Entry.getKey(), DigestUtils.md5DigestAsHex(bz2Entry.getValue()));
        }
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
