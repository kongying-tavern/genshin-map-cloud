package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.mapper.ItemTypeLinkMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.mapper.MarkerPunctuateMapper;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.PunctuateSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.PunctuateAuditService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.primitives.Booleans.countTrue;

/**
 * 打点审核服务接口实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PunctuateAuditServiceImpl implements PunctuateAuditService {

    private final MarkerMapper markerMapper;
    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;

    /**
     * 根据各种条件筛选打点ID
     *
     * @param searchVo 打点查询前端封装
     * @return 打点ID列表
     */
    @Override
    @Cacheable("searchPunctuateId")
    public List<Long> searchPunctuateId(PunctuateSearchVo searchVo) {
        boolean isAuthor = !(searchVo.getAuthorList() == null || searchVo.getAuthorList().isEmpty());
        //TODO 重复代码优化
        boolean isArea = !(searchVo.getAreaIdList() == null || searchVo.getAreaIdList().isEmpty());
        boolean isItem = !(searchVo.getItemIdList() == null || searchVo.getItemIdList().isEmpty());
        boolean isType = !(searchVo.getTypeIdList() == null || searchVo.getTypeIdList().isEmpty());


        if (countTrue(isArea, isItem, isType) > 1)
            throw new RuntimeException("条件冲突");
        List<Long> itemIdList = new ArrayList<>();
        //根据地区，类型来筛选出需要的物品id，如果直接是物品id则直接使用提交的物品id
        if (isArea) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getAreaId, searchVo.getAreaIdList())
                            .select(Item::getId))
                    .parallelStream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }
        if (isItem) {
            itemIdList = searchVo.getItemIdList();
        }
        if (isType) {
            itemIdList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                            .in(ItemTypeLink::getTypeId, searchVo.getTypeIdList())
                            .select(ItemTypeLink::getItemId))
                    .parallelStream()
                    .map(ItemTypeLink::getItemId).distinct().collect(Collectors.toList());
        }
        //如果上面的筛选都没有，则只筛选作者
        if (itemIdList.isEmpty()) {
            if (!isAuthor) return new ArrayList<>();
            return markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery()
                            .in(isAuthor, MarkerPunctuate::getAuthor, searchVo.getAuthorList()))
                    .stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList());
        }
        List<Long> result = new ArrayList<>();
        itemIdList.parallelStream().forEach(itemId ->
                result.addAll(markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .in(isAuthor, MarkerPunctuate::getAuthor, searchVo.getAuthorList())
                                //TODO:需要注意库中究竟存了什么
                                .apply("json_contains(item_list,{0})", "{\"itemId\": " + itemId + "}")
                                .select(MarkerPunctuate::getPunctuateId))
                        .stream()
                        .map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList()))
        );
        return result.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 根据各种条件筛选打点信息
     *
     * @param punctuateSearchVo 打点查询前端封装
     * @return 打点ID列表
     */
    @Override
    //此处是两个方法的缝合，不需要加缓存
    public List<MarkerPunctuateDto> searchPunctuate(PunctuateSearchVo punctuateSearchVo) {
        List<Long> punctuateIdList = searchPunctuateId(punctuateSearchVo);
        return listPunctuateById(punctuateIdList);
    }

    /**
     * 通过打点ID列表查询打点信息
     *
     * @param punctuateIdList 打点ID列表
     * @return 打点完整信息的数据封装列表
     */
    @Override
    @Cacheable("listPunctuateById")
    public List<MarkerPunctuateDto> listPunctuateById(List<Long> punctuateIdList) {
        if (punctuateIdList.isEmpty()) {
            return new ArrayList<>();
        }
        return markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, punctuateIdList))
                .parallelStream().map(MarkerPunctuateDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询所有打点信息（包括暂存）
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的前端分页记录封装
     */
    @Override
    @Cacheable("listAllPunctuatePage")
    public PageListVo<MarkerPunctuateVo> listAllPunctuatePage(PageSearchDto pageSearchDto) {
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.lambdaQuery());
        List<Long> punctuateIdList = punctuatePage.getRecords().stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList());

        if (punctuateIdList.isEmpty()) {
            return new PageListVo<MarkerPunctuateVo>().setRecord(new ArrayList<>())
                    .setSize(punctuatePage.getSize())
                    .setTotal(punctuatePage.getTotal());
        }
        return new PageListVo<MarkerPunctuateVo>()
                .setRecord(punctuatePage.getRecords()
                        .parallelStream()
                        .map(MarkerPunctuateDto::new)
                        .map(MarkerPunctuateDto::getVo)
                        .collect(Collectors.toList()))
                .setSize(punctuatePage.getSize())
                .setTotal(punctuatePage.getTotal());
    }

    /**
     * 通过点位审核
     *
     * @param punctuateId 打点ID
     * @return 点位ID
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
    public Long passPunctuate(Long punctuateId) {
        //打点信息
        MarkerPunctuate markerPunctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId)))
                .orElseThrow(() -> new RuntimeException("无打点相关信息，请联系管理员"));
        Integer methodType = markerPunctuate.getMethodType();
        if (methodType == null) {
            throw new RuntimeException("无打点操作类型，请联系管理员");
        }
        //删除操作
        if (methodType.equals(3)) {
            //获取原有点位id
            Long originalMarkerId = Optional.ofNullable(markerPunctuate.getOriginalMarkerId())
                    .orElseThrow(() -> new RuntimeException("无法找到修改点位的原始id，请联系管理员"));
            //删除
            markerMapper.deleteById(originalMarkerId);
            //清除提交信息
            markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
        }
        //修改操作，只对自身和各个打点表内存在的信息做更新
        if (methodType.equals(2)) {
            //获取原有点位id
            Long originalMarkerId = Optional.ofNullable(markerPunctuate.getOriginalMarkerId())
                    .orElseThrow(() -> new RuntimeException("无法找到修改点位的原始id，请联系管理员"));
            //根据ID查询原有点位
            Marker oldMarker = Optional.ofNullable(markerMapper.selectOne(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, originalMarkerId)))
                    .orElseThrow(() -> new RuntimeException("无法找到原始id对应的原始点位，无法做出更改，请联系管理员"));
            //原有点位拷贝一份作为新点位
            Marker newMarker = BeanUtils.copyProperties(oldMarker, Marker.class);
            //打点的更改信息复制到新点位中（使用了hutool的copy，忽略null值）
            BeanUtils.copyNotNull(markerPunctuate, newMarker);
            markerMapper.updateById(newMarker);
            return newMarker.getId();
        }
        //新增操作
        if (methodType.equals(1)) {
            //插入自身
            Marker marker = BeanUtils.copyProperties(markerPunctuate, Marker.class)
                    //ID应为空
                    .setId(null);
            markerMapper.insert(marker);
            //清除提交信息
            markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
            return marker.getId();
        }
        throw new RuntimeException("无效操作类型，请联系管理员");
    }

    /**
     * 驳回点位审核
     *
     * @param punctuateId 打点ID
     * @return 是否成功
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
    public Boolean rejectPunctuate(Long punctuateId) {
        MarkerPunctuate markerPunctuate = markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
        markerPunctuateMapper.updateById(markerPunctuate.setStatus(0));
        return true;
    }

    /**
     * 删除提交点位
     *
     * @param punctuateId 打点ID
     * @return 是否成功
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
    public Boolean deletePunctuate(Long punctuateId) {
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
        return true;
    }

}
