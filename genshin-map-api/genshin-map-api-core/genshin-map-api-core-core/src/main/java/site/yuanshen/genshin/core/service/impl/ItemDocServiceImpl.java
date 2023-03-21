package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.yuanshen.genshin.core.dao.ItemDao;
import site.yuanshen.genshin.core.service.ItemDocService;

/**
 * 物品压缩档案服务层实现
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemDocServiceImpl implements ItemDocService {

    private final ItemDao itemDao;

    /**
     * 生成物品的bz2压缩字节数组
     *
     * @return 字节数组的md5
     */
    @Override
    @Cacheable("listItemBz2MD5")
    public String listItemBz2MD5() {
        return "缓存未生成或生成失败";
    }

    /**
     * 刷新物品分页bz2和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @Override
    @CachePut("listItemBz2MD5")
    public String refreshItemBz2MD5() {
        long startTime = System.currentTimeMillis();
        String result = itemDao.refreshAllItemBz2();
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
