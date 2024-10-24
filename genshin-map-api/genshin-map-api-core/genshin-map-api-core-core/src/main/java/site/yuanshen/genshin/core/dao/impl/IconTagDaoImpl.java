package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.genshin.core.dao.IconTagDao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图标标签的数据查询层实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class IconTagDaoImpl implements IconTagDao {

    private final CacheManager cacheManager;
    private final TagMapper tagMapper;
    private final TagTypeLinkMapper tagTypeLinkMapper;
    private final IconMapper iconMapper;

    /**
     * @return 所有的标签信息
     */
    @Override
    @Cacheable(value = "listAllTag")
    public List<TagVo> listAllTag() {
        //按照条件进行筛选
        List<Tag> tagPage = tagMapper.selectList(Wrappers.query());
        List<TagDto> tagDtoList = tagPage
                .stream().map(TagDto::new).collect(Collectors.toList());
        List<String> tagNameList = tagDtoList.stream()
                .map(TagDto::getTag).collect(Collectors.toList());

        //收集分类信息
        Map<String, List<Long>> typeMap = new HashMap<>();
        tagTypeLinkMapper.selectList(Wrappers.<TagTypeLink>lambdaQuery()
                        .in(TagTypeLink::getTagName, tagNameList))
                .forEach(typeLink -> {
                    List<Long> tempList = typeMap.getOrDefault(typeLink.getTagName(), new ArrayList<>());
                    tempList.add(typeLink.getTypeId());
                    typeMap.put(typeLink.getTagName(), tempList);
                });
        //收集图标信息
        List<Long> iconIdList = tagDtoList.stream().map(TagDto::getIconId).distinct().collect(Collectors.toList());
        Map<Long, String> urlMap = iconMapper.selectList(Wrappers.<Icon>lambdaQuery().in(Icon::getId, iconIdList))
                .stream().collect(Collectors.toMap(Icon::getId, Icon::getUrl));
        return tagDtoList.stream().map(dto ->
                                dto.getVo()
                                        .withTypeIdList(typeMap.getOrDefault(dto.getTag(), new ArrayList<>()))
                                        .withUrl(urlMap.getOrDefault(dto.getIconId(), "")))
                        .collect(Collectors.toList());
    }

    /**
     * @return 所有的标签信息的压缩
     */
    @Override
    @Cacheable("listAllTagBinary")
    public byte[] listAllTagBinary() {
        try {
            return CompressUtils.compress(JSON.toJSONString(
                            listAllTag())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败" + e);
        }
    }

    /**
     * @return 所有的标签信息的压缩的md5
     */
    @Override
    @Cacheable("listAllTagBinaryMd5")
    public String listAllTagBinaryMd5() {
        CaffeineCache tagBinaryCache = (CaffeineCache) cacheManager.getCache("listAllTag");
        byte[] allTagBinary;
        if (tagBinaryCache != null) {
            if (!tagBinaryCache.getNativeCache().asMap().isEmpty()) {
                allTagBinary = (byte[]) tagBinaryCache.getNativeCache().getIfPresent("allTagBinary");
                if (allTagBinary == null) {
                    tagBinaryCache.evict("allTagBinary");
                    allTagBinary = listAllTagBinary();
                }
            } else {
                tagBinaryCache.evict("allTagBinary");
                allTagBinary = listAllTagBinary();
            }
        } else {
            allTagBinary = listAllTagBinary();
        }
        return DigestUtils.md5DigestAsHex(allTagBinary);
    }
}
