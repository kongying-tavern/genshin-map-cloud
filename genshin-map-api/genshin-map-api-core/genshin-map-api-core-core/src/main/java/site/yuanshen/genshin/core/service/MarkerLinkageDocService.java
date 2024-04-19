package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;

/**
 * 点位关联压缩档案服务层实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerLinkageDocService {
    private final MarkerLinkageDao markerLinkageDao;

    /**
     * 生成点位关联列表的压缩字节数组
     *
     * @return 字节数组的md5
     */
    @Cacheable(value = "listMarkerLinkageBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String listMarkerLinkageBinaryMD5() {
        return "缓存未生成或生成失败";
    }

    /**
     * 刷新点位关联列表和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @CachePut(value = "listMarkerLinkageBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String refreshMarkerLinkageListBinaryMD5() {
        final long startTime = System.currentTimeMillis();
        final String result = DigestUtils.md5DigestAsHex(markerLinkageDao.refreshAllMarkerLinkageListBinary());
        log.info("点位关联列表MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }

    /**
     * 生成点位关联有向图的压缩字节数组
     *
     * @return 字节数组的md5
     */
    @Cacheable(value = "graphMarkerLinkageBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String graphMarkerLinkageBinaryMD5() {
        return "缓存未生成或生成失败";
    }

    /**
     * 刷新点位关联有向图和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @CachePut(value = "graphMarkerLinkageBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String refreshMarkerLinkageGraphBinaryMD5() {
        final long startTime = System.currentTimeMillis();
        final String result = DigestUtils.md5DigestAsHex(markerLinkageDao.refreshAllMarkerLinkageGraphBinary());
        log.info("点位关联有向图MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
