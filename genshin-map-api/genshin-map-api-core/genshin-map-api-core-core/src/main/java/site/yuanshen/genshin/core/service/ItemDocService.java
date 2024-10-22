package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.genshin.core.dao.ItemDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<String> listItemBinaryMD5() {
        return List.of();
    }

    public Map<String, String> refreshItemBinaryMD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new LinkedHashMap<>();
        itemDao.refreshItemBinaryList();
        log.info("物品MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
