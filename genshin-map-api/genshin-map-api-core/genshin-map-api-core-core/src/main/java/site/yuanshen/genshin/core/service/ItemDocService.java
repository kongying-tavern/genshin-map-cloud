package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.genshin.core.dao.ItemDao;

/**
 * 物品压缩档案服务层实现
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemDocService {

    private final ItemDao itemDao;

    /**
     * 生成物品的压缩字节数组
     *
     * @return 字节数组的md5
     */
    @Cacheable(value = "listItemBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String listItemBinaryMD5() {
        return "缓存未生成或生成失败";
    }

    /**
     * 刷新物品分页和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @CachePut(value = "listItemBinaryMD5", cacheManager = "neverRefreshCacheManager")
    public String refreshItemBinaryMD5() {
        long startTime = System.currentTimeMillis();
        String result = DigestUtils.md5DigestAsHex(itemDao.refreshAllItemBinary());
        log.info("物品MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
