package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.dto.IconDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.IconTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.IconTypeLinkMapper;
import site.yuanshen.data.vo.BinaryMD5Vo;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.genshin.core.dao.IconDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图标的数据查询层实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class IconDaoImpl implements IconDao {

    private final CacheManager cacheManager;
    private final IconMapper iconMapper;
    private final IconTypeLinkMapper iconTypeLinkMapper;

    /**
     * @return 所有的图标信息
     */
    @Override
    @Cacheable(value = "listAllIcon")
    public List<IconVo> listAllIcon() {
        Cache listAllTagBinaryMd5GenerateTimestamp = getListAllTagBinaryMd5GenerateTimestamp();

        // 按照条件进行筛选
        List<Icon> iconPage = iconMapper.selectList(Wrappers.query());
        List<IconDto> iconDtoList = iconPage
            .stream().map(IconDto::new).collect(Collectors.toList());
        List<Long> iconIdList = iconDtoList.stream()
            .map(IconDto::getId).collect(Collectors.toList());

        // 收集分类信息
        Map<Long, List<Long>> typeMap = new HashMap<>();
        iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                .in(IconTypeLink::getIconId, iconIdList))
            .forEach(typeLink -> {
                List<Long> tempList = typeMap.getOrDefault(typeLink.getIconId(), new ArrayList<>());
                tempList.add(typeLink.getTypeId());
                typeMap.put(typeLink.getIconId(), tempList);
            });

        listAllTagBinaryMd5GenerateTimestamp.clear();
        listAllTagBinaryMd5GenerateTimestamp.put("", TimeUtils.getCurrentTimestamp().getTime());

        return iconDtoList.stream().map(dto ->
                dto.getVo()
                    .withTypeIdList(typeMap.getOrDefault(dto.getId(), new ArrayList<>()))
            )
            .collect(Collectors.toList());
    }

    /**
     * @return 所有的图标信息的压缩
     */
    @Override
    @Cacheable("listAllIconBinary")
    public byte[] listAllIconBinary() {
        try {
            return CompressUtils.compress(JSON.toJSONString(
                    listAllIcon())
                .getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败" + e);
        }
    }

    /**
     * @return 所有的图标信息的压缩的md5
     */
    @Override
    @Cacheable("listAllIconBinaryMd5")
    public BinaryMD5Vo listAllIconBinaryMd5() {
        CaffeineCache iconBinaryCache = (CaffeineCache) cacheManager.getCache("listAllIcon");
        CaffeineCache binaryMd5GenerateTimestampCache = (CaffeineCache) cacheManager.getCache("listAllTagBinaryMd5GenerateTimestamp");
        byte[] allTagBinary;
        if (iconBinaryCache != null) {
            if (!iconBinaryCache.getNativeCache().asMap().isEmpty()) {
                allTagBinary = (byte[]) iconBinaryCache.getNativeCache().getIfPresent("allTagBinary");
                if (allTagBinary == null) {
                    iconBinaryCache.evict("allTagBinary");
                    allTagBinary = listAllIconBinary();
                }
            } else {
                iconBinaryCache.evict("allTagBinary");
                allTagBinary = listAllIconBinary();
            }
        } else {
            allTagBinary = listAllIconBinary();
        }

        Long time = Optional.ofNullable(binaryMd5GenerateTimestampCache.getNativeCache().getIfPresent(""))
            .map(v -> NumberUtils.createLong(v.toString()))
            .orElse(null);

        BinaryMD5Vo binaryMD5Vo = new BinaryMD5Vo();
        binaryMD5Vo.setTime(time);
        binaryMD5Vo.setMd5(DigestUtils.md5DigestAsHex(allTagBinary));
        return binaryMD5Vo;
    }

    private Cache getListAllTagBinaryMd5GenerateTimestamp() {
        final Cache listAllTagBinaryMd5GenerateTimestamp = cacheManager.getCache("listAllTagBinaryMd5GenerateTimestamp");
        if (listAllTagBinaryMd5GenerateTimestamp == null)
            throw new GenshinApiException("缓存未初始化");
        return listAllTagBinaryMd5GenerateTimestamp;
    }
}
