package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.util.LinkedHashMap;
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

    public List<String> listMarkerBinaryMD5() {
        return List.of();
    }

    public Map<String, String> refreshMarkerBinaryMD5() {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new LinkedHashMap<>();
        markerDao.refreshMarkerBinaryList();
        log.info("点位MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        return result;
    }
}
