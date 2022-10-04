package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.data.mapper.TagTypeMapper;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.genshin.core.dao.IconTagDao;
import site.yuanshen.genshin.core.service.mbp.TagMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeMBPService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
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
        Map<Long, String> urlMap = iconMapper.selectList(Wrappers.<Icon>lambdaQuery().in(Icon::getIconId, iconIdList))
                .stream().collect(Collectors.toMap(Icon::getIconId, Icon::getUrl));
        return tagDtoList.stream().map(dto ->
                                dto.setTypeIdList(typeMap.getOrDefault(dto.getTag(), new ArrayList<>()))
                                        .setUrl(urlMap.getOrDefault(dto.getIconId(), ""))
                                        .getVo())
                        .collect(Collectors.toList());
    }

    /**
     * @return 所有的标签信息的Bz2压缩
     */
    @Override
    @Cacheable("listAllTagBz2")
    public byte[] listAllTagBz2() {
        try {
            return CompressUtils.compress(JSON.toJSONString(
                            listAllTag())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("创建压缩失败" + e);
        }
    }

    /**
     * @return 所有的标签信息的Bz2压缩的md5
     */
    @Override
    @Cacheable("listAllTagBz2Md5")
    public String listAllTagBz2Md5() {
        CaffeineCache tagBz2Cache = (CaffeineCache) cacheManager.getCache("listAllTag");
        byte[] allTagBz2;
        if (tagBz2Cache != null) {
            if (!tagBz2Cache.getNativeCache().asMap().isEmpty()) {
                allTagBz2 = (byte[]) tagBz2Cache.getNativeCache().getIfPresent("allTagBz2");
                if (allTagBz2 == null) {
                    tagBz2Cache.evict("allTagBz2");
                    allTagBz2 = listAllTagBz2();
                }
            } else {
                tagBz2Cache.evict("allTagBz2");
                allTagBz2 = listAllTagBz2();
            }
        } else {
            allTagBz2 = listAllTagBz2();
        }
        return DigestUtils.md5DigestAsHex(allTagBz2);
    }
}
