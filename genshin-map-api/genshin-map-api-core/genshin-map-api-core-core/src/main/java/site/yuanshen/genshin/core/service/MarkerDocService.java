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
     * 返回点位分页的md5数组
     *
     * @return 分页字节数组的md5
     */
    @Cacheable(value = "listMarkerBinaryMD5", key = "''", cacheManager = "neverRefreshCacheManager")
    public Map<String, String> listAllMarkerBinaryMD5() {
        return new LinkedHashMap<>();
    }

    @CachePut(value = "listMarkerBinaryMD5", key = "''", cacheManager = "neverRefreshCacheManager")
    public Map<String, String> refreshMarkerBinaryMD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, byte[]> binaryMap = markerDao.refreshPageMarkerByBinary();
        for(Map.Entry<String, byte[]> binaryEntry : binaryMap.entrySet()) {
            result.put(binaryEntry.getKey(), DigestUtils.md5DigestAsHex(binaryEntry.getValue()));
        }
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
