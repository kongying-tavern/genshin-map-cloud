package site.yuanshen.genshin.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.CachedBeanCopier;
import site.yuanshen.data.dto.*;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.*;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.MarkerService;
import site.yuanshen.genshin.core.service.mbp.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.primitives.Booleans.countTrue;

/**
 * 点位服务接口实现
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerServiceImpl implements MarkerService {

    private final MarkerMapper markerMapper;
    private final MarkerMBPService markerMBPService;
    private final MarkerExtraMapper markerExtraMapper;
    private final MarkerExtraMBPService markerExtraMBPService;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerItemLinkMBPService markerItemLinkMBPService;
    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final MarkerPunctuateMBPService markerPunctuateMBPService;
    private final MarkerExtraPunctuateMapper markerExtraPunctuateMapper;
    private final MarkerExtraPunctuateMBPService markerExtraPunctuateMBPService;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;

    /**
     * 根据各种条件筛选查询点位ID
     *
     * @param searchVo 点位查询前端封装
     * @return 点位ID列表
     */
    @Override
    public List<Long> searchMarkerId(MarkerSearchVo searchVo) {
        boolean isArea = !(searchVo.getAreaIdList() == null || searchVo.getAreaIdList().isEmpty());
        boolean isItem = !(searchVo.getItemIdList() == null || searchVo.getItemIdList().isEmpty());
        boolean isType = !(searchVo.getTypeIdList() == null || searchVo.getTypeIdList().isEmpty());
        if (isArea && (isItem || isType) || isItem && isType)
            throw new RuntimeException("条件冲突");
        List<Long> itemIdList = new ArrayList<>();
        if (isArea) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getAreaId, searchVo.getAreaIdList())
                            .select(Item::getId))
                    .stream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }
        if (isItem) {
            itemIdList = searchVo.getItemIdList();
        }
        if (isType) {
            itemIdList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                            .in(ItemTypeLink::getTypeId, searchVo.getTypeIdList())
                            .select(ItemTypeLink::getItemId))
                    .stream()
                    .map(ItemTypeLink::getItemId).distinct().collect(Collectors.toList());
        }
        if (!searchVo.getGetBeta()) {
            if (itemIdList.isEmpty()) return new ArrayList<>();
            return markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
                            .in(MarkerItemLink::getItemId, itemIdList)
                            .select(MarkerItemLink::getMarkerId))
                    .stream()
                    .map(MarkerItemLink::getMarkerId)
                    .distinct().collect(Collectors.toList());
        } else {
            List<Long> result = new ArrayList<>();
            itemIdList.parallelStream().forEach(itemId ->
                    result.addAll(markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery()
                                    .apply("json_contains(item_list,{0})", "\"" + itemId.toString() + "\"")
                                    .select(MarkerPunctuate::getPunctuateId))
                            .stream()
                            .map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList()))
            );
            return result.stream().distinct().collect(Collectors.toList());
        }
    }

    /**
     * 根据各种条件查询所有点位信息
     *
     * @param markerSearchVo 点位查询前端封装
     * @return 点位完整信息的数据封装列表
     */
    @Override
    public List<MarkerDto> searchMarker(MarkerSearchVo markerSearchVo) {
        List<Long> markerIdList = searchMarkerId(markerSearchVo);
        return listMarkerById(markerIdList);
    }


    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList 点位ID列表
     * @return 点位完整信息的数据封装列表
     */
    @Override
    public List<MarkerDto> listMarkerById(List<Long> markerIdList) {
        //为空直接返回
        if (markerIdList.isEmpty()) return new ArrayList<>();
        //获取所有的额外字段
        Map<Long, MarkerExtra> markerExtraMap = markerExtraMapper.selectList(Wrappers.<MarkerExtra>lambdaQuery().in(MarkerExtra::getMarkerId, markerIdList))
                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra));
        //获取关联的物品Id
        Map<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().in(MarkerItemLink::getMarkerId, markerIdList))
                .parallelStream().forEach(markerItemLink ->
                        itemLinkMap.compute(markerItemLink.getMarkerId(),
                                (markerId, linkList) -> {
                                    if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                                    linkList.add(markerItemLink);
                                    return linkList;
                                }));
        //构建返回
        return markerMapper.selectList(Wrappers.<Marker>lambdaQuery().in(Marker::getId, markerIdList))
                .parallelStream().map(marker ->
                        new MarkerDto(marker,
                                markerExtraMap.get(marker.getId()),
                                itemLinkMap.get(marker.getId()))).collect(Collectors.toList());
    }

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 点位完整信息的前端封装的分页记录
     */
    @Override
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto) {
        Page<Marker> markerPage = markerMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.lambdaQuery());
        List<Long> markerIdList = markerPage.getRecords().stream()
                .map(Marker::getId).collect(Collectors.toList());
        Map<Long, MarkerExtra> extraMap = markerExtraMapper.selectList(Wrappers.<MarkerExtra>lambdaQuery()
                        .in(MarkerExtra::getMarkerId, markerIdList))
                .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra));
        Map<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().in(MarkerItemLink::getMarkerId, markerIdList))
                .parallelStream().forEach(markerItemLink ->
                        itemLinkMap.compute(markerItemLink.getMarkerId(),
                                (markerId, linkList) -> {
                                    if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                                    linkList.add(markerItemLink);
                                    return linkList;
                                }));
        return new PageListVo<MarkerVo>()
                .setRecord(markerPage.getRecords().parallelStream()
                        .map(marker -> new MarkerDto(marker, extraMap.get(marker.getId()), itemLinkMap.get(marker.getId())).getVo())
                        .collect(Collectors.toList()))
                .setTotal(markerPage.getTotal())
                .setSize(markerPage.getSize());
    }

    /**
     * 新增点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 新点位ID
     */
    @Override
    public Long createMarker(MarkerSingleDto markerSingleDto) {
        Marker marker = markerSingleDto.getEntity();
        markerMapper.insert(marker);
        //正式更新id
        List<MarkerItemLink> itemLinkList = markerSingleDto.getItemList().parallelStream().map(markerItemLinkDto -> markerItemLinkDto.getEntity().setMarkerId(marker.getId())).collect(Collectors.toList());
        markerItemLinkMBPService.saveBatch(itemLinkList);
        return marker.getId();
    }

    /**
     * 新增点位额外字段信息
     *
     * @param markerExtraDto 点位额外信息的数据封装
     * @return 是否成功
     */
    @Override
    public Boolean addMarkerExtra(MarkerExtraDto markerExtraDto) {
        return markerExtraMapper.insert(markerExtraDto.getEntity()) == 1;
    }

    /**
     * 修改点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 是否成功
     */
    @Override
    public Boolean updateMarker(MarkerSingleDto markerSingleDto) {
        if (markerSingleDto.getItemList() != null || !markerSingleDto.getItemList().isEmpty()) {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerSingleDto.getMarkerId()));
            List<MarkerItemLink> itemLinkList = markerSingleDto.getItemList().parallelStream().map(markerItemLinkDto -> markerItemLinkDto.getEntity().setMarkerId(markerSingleDto.getMarkerId())).collect(Collectors.toList());
            markerItemLinkMBPService.saveBatch(itemLinkList);
        } else {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerSingleDto.getMarkerId()));
        }
        return markerMapper.update(markerSingleDto.getEntity(), Wrappers.<Marker>lambdaUpdate()
                .eq(Marker::getId, markerSingleDto.getMarkerId())) == 1;
    }

    /**
     * 修改点位额外字段
     *
     * @param markerExtraDto 点位额外信息的数据封装
     * @return 是否成功
     */
    @Override
    public Boolean updateMarkerExtra(MarkerExtraDto markerExtraDto) {
        return markerExtraMapper.update(markerExtraDto.getEntity(), Wrappers.<MarkerExtra>lambdaUpdate()
                .eq(MarkerExtra::getMarkerId, markerExtraDto.getMarkerId())) == 1;
    }

    /**
     * 根据点位ID列表批量删除点位
     *
     * @param markerIdList 点位ID列表
     * @return 是否成功
     */
    @Override
    public Boolean deleteMarker(List<Long> markerIdList) {
        markerMapper.delete(Wrappers.<Marker>lambdaQuery().in(Marker::getId, markerIdList));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().in(MarkerItemLink::getMarkerId, markerIdList));
        markerExtraMapper.delete(Wrappers.<MarkerExtra>lambdaQuery().in(MarkerExtra::getMarkerId, markerIdList));
        return true;
    }

    /**
     * 根据各种条件筛选打点ID
     *
     * @param searchVo 打点查询前端封装
     * @return 打点ID列表
     */
    @Override
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
    public List<MarkerPunctuateDto> listPunctuateById(List<Long> punctuateIdList) {
        if (punctuateIdList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, MarkerExtraPunctuate> markerExtraMap = markerExtraPunctuateMapper.selectList(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                        .in(MarkerExtraPunctuate::getPunctuateId, punctuateIdList))
                .stream().collect(Collectors.toMap(MarkerExtraPunctuate::getPunctuateId, extraPunctuate -> extraPunctuate));
        return markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, punctuateIdList))
                .parallelStream().map(punctuate ->
                        new MarkerPunctuateDto(punctuate,
                                markerExtraMap.get(punctuate.getPunctuateId())))
                .collect(Collectors.toList());
    }

    /**
     * 分页查询所有打点信息（包括暂存）
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的前端分页记录封装
     */
    @Override
    public PageListVo<MarkerPunctuateVo> listAllPunctuatePage(PageSearchDto pageSearchDto) {
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.lambdaQuery());
        List<Long> punctuateIdList = punctuatePage.getRecords().stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList());

        if (punctuateIdList.isEmpty()) {
            return new PageListVo<MarkerPunctuateVo>().setRecord(new ArrayList<>())
                    .setSize(punctuatePage.getSize())
                    .setTotal(punctuatePage.getTotal());
        }

        Map<Long, MarkerExtraPunctuate> extraPunctuateMap = markerExtraPunctuateMapper.selectList(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                        .in(MarkerExtraPunctuate::getPunctuateId, punctuateIdList))
                .stream().collect(Collectors.toMap(MarkerExtraPunctuate::getPunctuateId, extraPunctuate -> extraPunctuate));
        return new PageListVo<MarkerPunctuateVo>()
                .setRecord(punctuatePage.getRecords().parallelStream()
                        .map(punctuate -> new MarkerPunctuateDto(punctuate, extraPunctuateMap.get(punctuate.getPunctuateId()))
                                .getVo()).collect(Collectors.toList()))
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
    public Long passPunctuate(Long punctuateId) {
        //打点信息
        MarkerPunctuate markerPunctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId)))
                .orElseThrow(() -> new RuntimeException("无打点相关信息，请联系管理员"));
        //打点额外信息，保留optional用于判空
        Optional<MarkerExtraPunctuate> markerExtraPunctuateOptional = Optional.ofNullable(
                markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, punctuateId)));
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
            markerExtraMapper.delete(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, originalMarkerId));
            //清除提交信息
            markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
            markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, punctuateId));
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
            Marker newMarker = CachedBeanCopier.copyProperties(oldMarker, Marker.class);
            //打点的更改信息复制到新点位中（使用了hutool的copy，忽略null值）
            BeanUtil.copyProperties(markerPunctuate, newMarker, CopyOptions.create().ignoreNullValue().ignoreError());
            markerMapper.updateById(newMarker);
            //是否有额外字段
            if (markerExtraPunctuateOptional.isPresent()) {
                MarkerExtraPunctuate markerExtraPunctuate = markerExtraPunctuateOptional.get();
                //根据ID查询原有点位的额外信息
                MarkerExtra markerExtra = Optional.ofNullable(markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, originalMarkerId)))
                        .orElseThrow(() -> new RuntimeException("无原始的额外字段，请先对点位进行插入操作"));
                //打点的额外信息更改复制到点位中（使用了hutool的copy，忽略null值）
                BeanUtil.copyProperties(markerExtraPunctuate, markerExtra, CopyOptions.create().ignoreNullValue().ignoreError());
                //更新额外信息
                markerExtraMapper.updateById(markerExtra);
                //查看是否有关联点位需要同步更改通过
                if (Boolean.TRUE.equals(markerExtraPunctuate.getIsRelated())) {
                    if (markerExtraPunctuate.getParentId() != null) {
                        throw new RuntimeException("通过的关联点位不是父点位或随机组内的点位，请选择父点位进行通过");
                    }
                    //根据relate_type来判断生成allPunctuateIdList
                    List<Long> allPunctuateIdList = null;
                    try {
                        allPunctuateIdList = getAllRelateIds(markerExtraPunctuate.getPunctuateId(), markerExtraPunctuate);
                    } catch (Exception e) {
                        throw new RuntimeException("关联点位解析失败，请检查json");
                    }
                    //对所有关联的点位进行更新操作
                    allPunctuateIdList.parallelStream()
                            .forEach(
                                    relatePunctuateId -> {
                                        Optional<MarkerPunctuate> relateMarkerPunctuateOptional = Optional.ofNullable(
                                                markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, relatePunctuateId)));
                                        Optional<MarkerExtraPunctuate> relateMarkerExtraPunctuateOptional = Optional.ofNullable(
                                                markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, relatePunctuateId)));
                                        if (relateMarkerPunctuateOptional.isEmpty() && relateMarkerExtraPunctuateOptional.isEmpty())
                                            log.debug("遍历 打点id为 {} ，原始id为 {} 的点位：打点id为 {} 关联打点点位未找到，跳过", punctuateId, newMarker.getId(), relatePunctuateId);
                                        MarkerPunctuate relateMarkerPunctuate = relateMarkerPunctuateOptional.orElse(new MarkerPunctuate());
                                        //关联点位有single信息，进行更新
                                        if (relateMarkerPunctuateOptional.isPresent()) {
                                            //获取原有点位id
                                            Long relateOriginalMarkerId = Optional.ofNullable(relateMarkerPunctuate.getOriginalMarkerId())
                                                    .orElseThrow(() -> new RuntimeException("无法找到修改点位的原始id，请联系管理员"));
                                            //根据ID查询原有点位
                                            Marker relateMarker = Optional.ofNullable(markerMapper.selectOne(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, relateOriginalMarkerId)))
                                                    .orElseThrow(() -> new RuntimeException("无法找到原始id对应的原始点位，无法做出更改，请联系管理员"));
                                            //打点的更改信息复制到点位中（使用了hutool的copy，忽略null值）
                                            BeanUtil.copyProperties(relateMarkerPunctuate, relateMarker, CopyOptions.create().ignoreNullValue().ignoreError());
                                            markerMapper.updateById(relateMarker);
                                            //关联点位有extra信息，更新
                                            if (relateMarkerExtraPunctuateOptional.isPresent()) {
                                                MarkerExtraPunctuate relateMarkerExtraPunctuate = relateMarkerExtraPunctuateOptional.get();
                                                //根据ID查询原有点位的额外信息
                                                MarkerExtra relateMarkerExtra = Optional.ofNullable(markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, relateOriginalMarkerId)))
                                                        .orElseThrow(() -> new RuntimeException("无原始的额外字段，请先对点位进行插入操作"));
                                                //打点的额外信息更改复制到点位中（使用了hutool的copy，忽略null值）
                                                BeanUtil.copyProperties(relateMarkerExtraPunctuate, relateMarkerExtra, CopyOptions.create().ignoreNullValue().ignoreError());
                                                markerExtraMapper.updateById(relateMarkerExtra);
                                            }
                                        }
                                    });
                    markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, allPunctuateIdList));
                    markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().in(MarkerExtraPunctuate::getPunctuateId, allPunctuateIdList));
                }
            }
            return newMarker.getId();
        }
        //新增操作
        if (methodType.equals(1)) {
            //插入自身
            Marker marker = CachedBeanCopier.copyProperties(markerPunctuate, Marker.class)
                    //ID应为空
                    .setId(null);
            markerMapper.insert(marker);
            //如果存在额外字段
            if (markerExtraPunctuateOptional.isPresent()) {
                MarkerExtraPunctuate extraPunctuate = markerExtraPunctuateOptional.get();
                if (extraPunctuate.getIsRelated() == null)
                    throw new RuntimeException("点位关联标志位缺失，请联系管理员");
                //如果额外字段中包含关联信息，则处理关联点位，！！注意！！此处逻辑不直接插入本点位的额外字段，而只是修改extraPunctuate的关联信息
                if (extraPunctuate.getIsRelated().equals(true)) {
                    if (extraPunctuate.getParentId() != null) {
                        throw new RuntimeException("通过的关联点位不是父点位或随机组内的点位，请选择父点位进行通过");
                    }
                    //用于存放关联点位的打点id与正式id的对应关系
                    Map<Long, Long> punctuateToOriginalIdMap = new TreeMap<>();
                    //存入本点位的id对应关系
                    punctuateToOriginalIdMap.put(punctuateId, marker.getId());
                    //根据relate_type来判断生成allPunctuateIdList
                    List<Long> relatePunctuateIdList;
                    try {
                        relatePunctuateIdList = getAllRelateIds(extraPunctuate.getPunctuateId(), extraPunctuate);
                    } catch (Exception e) {
                        throw new RuntimeException("关联点位解析失败，请检查json");
                    }
                    log.info("打点id为 {} 的点位的关联点位ID已找到，有：{}", punctuateId, relatePunctuateIdList);
                    if (relatePunctuateIdList.isEmpty()) throw new RuntimeException("关联点位为空，与关联标志位冲突");
                    //选取所有关联的提交点位
                    List<MarkerPunctuate> relatePunctuateList = markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, relatePunctuateIdList));
                    //选取所有关联的提交点位的额外字段
                    List<MarkerExtraPunctuate> relateExtraPunctuateList = markerExtraPunctuateMapper.selectList(Wrappers.<MarkerExtraPunctuate>lambdaQuery().in(MarkerExtraPunctuate::getPunctuateId, relatePunctuateIdList));
                    if (relatePunctuateIdList.size() != relatePunctuateList.size()) {
                        log.error("打点id为 {} 的点位的关联打点的打点信息缺失，已找到的打点id如下{}", punctuateId, relatePunctuateList.stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList()));
                        throw new RuntimeException("关联打点的打点信息缺失，请联系管理员");
                    }
                    if (relatePunctuateIdList.size() != relateExtraPunctuateList.size()) {
                        log.error("打点id为 {} 的点位的关联打点的打点额外信息缺失，已找到的打点id如下{}", punctuateId, relateExtraPunctuateList.stream().map(MarkerExtraPunctuate::getPunctuateId).collect(Collectors.toList()));
                        throw new RuntimeException("关联打点的打点额外信息缺失，请联系管理员");
                    }
                    //将本点位先剔除，避免重复插入，这种情况主要对应处理关联类型为random_one的点位
                    relatePunctuateList = relatePunctuateList.stream().filter(punctuate -> !punctuate.getPunctuateId().equals(punctuateId)).collect(Collectors.toList());
                    //将关联的提交点位复制到正式点位表，并存入提交ID与点位ID的映射关系
                    relatePunctuateList.forEach(punctuate -> {
                        Marker relateMarker = CachedBeanCopier.copyProperties(punctuate, Marker.class);
                        markerMapper.insert(relateMarker);
                        punctuateToOriginalIdMap.put(punctuate.getPunctuateId(), relateMarker.getId());
                    });
                    JSONObject extraContent;
                    //用于存放关联点位的额外字段实体类的列表
                    List<MarkerExtra> relateMarkerExtraList = new ArrayList<>();
                    try {
                        extraContent = JSON.parseObject(extraPunctuate.getMarkerExtraContent());
                        List<Long> relate_list = extraContent.getJSONArray("relate_list").toJavaList(Long.class);
                        //根据relate_type来判断生成relate_list
                        switch (extraContent.getString("relate_type")) {
                            case "parent":
                                //将relate_list其中的打点id转换为正式id
                                relate_list = relate_list.stream().peek(punctuateToOriginalIdMap::get).collect(Collectors.toList());
                                extraContent.put("relate_list", relate_list);
                                //将关联点位的额外字段处理后放入实体类列表
                                for (MarkerExtraPunctuate markerExtraPunctuate : relateExtraPunctuateList) {
                                    //解析额外字段json，并将转化好的的关联字段存入json，此处只需要清除子点位的关联列表即可
                                    JSONObject contentJson = JSON.parseObject(markerExtraPunctuate.getMarkerExtraContent());
                                    //TODO 此处可加入子点位的关联类型校验
                                    contentJson.remove("relate_list");
                                    relateMarkerExtraList.add(new MarkerExtra()
                                            .setMarkerId(punctuateToOriginalIdMap.get(markerExtraPunctuate.getPunctuateId()))
                                            .setIsRelated(true)
                                            .setParentId(punctuateToOriginalIdMap.get(punctuateId))
                                            .setMarkerExtraContent(contentJson.toJSONString()));
                                }
                                break;
                            case "random_one":
                                //加回本点位，因为之前已经剔除
                                relate_list.add(punctuateId);
                                //将relate_list其中的打点id转换为正式id
                                relate_list = relate_list.stream().peek(punctuateToOriginalIdMap::get).collect(Collectors.toList());
                                extraContent.put("relate_list", relate_list);
                                //将关联点位的额外字段处理后放入实体类列表
                                for (MarkerExtraPunctuate markerExtraPunctuate : relateExtraPunctuateList) {
                                    //解析额外字段json，并将转化好的的关联字段存入json，此处的关联列表和本点位一致
                                    JSONObject contentJson = JSON.parseObject(markerExtraPunctuate.getMarkerExtraContent());
                                    //同组点位的关联类型校验
                                    if ("random_one".equals(contentJson.getString("relate_type")))
                                        throw new RuntimeException("同组点位，打点ID：" + markerExtraPunctuate.getPunctuateId() + " 的关联类型错误，请检查该点位的JSON");
                                    //存入关联列表
                                    contentJson.put("relate_list", relate_list);
                                    relateMarkerExtraList.add(new MarkerExtra()
                                            .setMarkerId(punctuateToOriginalIdMap.get(markerExtraPunctuate.getPunctuateId()))
                                            .setIsRelated(true)
                                            .setParentId(punctuateToOriginalIdMap.get(punctuateId))
                                            .setMarkerExtraContent(contentJson.toJSONString()));
                                }
                                break;
                            default:
                                throw new RuntimeException("非法的关联类型");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("刷新额外字段时，点位额外字段解析失败，请检查json，额外信息：" + e.getMessage());
                    }
                    if (extraContent == null || relateMarkerExtraList.isEmpty()) {
                        throw new RuntimeException("刷新额外字段时，点位额外字段解析失败，请联系管理员");
                    }
                    //将额外字段写回本点位
                    extraPunctuate.setMarkerExtraContent(extraContent.toJSONString());
                    //提交关联点位的额外字段
                    markerExtraMBPService.saveBatch(relateMarkerExtraList);
                    //清除关联点位的提交信息
                    markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, relatePunctuateIdList));
                    markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().in(MarkerExtraPunctuate::getPunctuateId, relatePunctuateIdList));
                }
            }
            //清除提交信息
            markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
            markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, punctuateId));
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
    public Boolean rejectPunctuate(Long punctuateId) {
        MarkerPunctuate markerPunctuate = markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
        MarkerExtraPunctuate markerExtraPunctuate = Optional.ofNullable(markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, punctuateId)))
                .orElse(new MarkerExtraPunctuate());
        //todo json判空的异常处理
        //当关联了其他的提交点位时，需要同步通过关联点位
        if (Boolean.TRUE.equals(markerExtraPunctuate.getIsRelated())) {
            //根据relate_type来判断生成allPunctuateIdList
            List<Long> allPunctuateIdList = getAllRelateIds(markerPunctuate.getPunctuateId(), markerExtraPunctuate);
            markerPunctuateMapper.update(null, Wrappers.<MarkerPunctuate>lambdaUpdate()
                    .in(MarkerPunctuate::getPunctuateId, allPunctuateIdList)
                    //TODO 将status做成常量
                    .set(MarkerPunctuate::getStatus, 0));
            return true;
        }
        markerPunctuateMapper.updateById(markerPunctuate.setStatus(0));
        return true;
    }

    /**
     * 删除提交点位
     *
     * @param punctuateIdList 打点ID列表
     * @return 是否成功
     */
    @Override
    public Boolean deletePunctuate(List<Long> punctuateIdList) {
        Set<Long> allPunctuateIdList = new HashSet<>();
        punctuateIdList.parallelStream().forEach(punctuateId -> allPunctuateIdList.addAll(getAllRelateIds(punctuateId)));
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().in(MarkerPunctuate::getPunctuateId, allPunctuateIdList));
        markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().in(MarkerExtraPunctuate::getPunctuateId, allPunctuateIdList));
        return true;
    }

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    @Override
    public PageListVo<MarkerPunctuateVo> listPunctuatePage(PageSearchDto pageSearchDto) {
        //TODO 将status做成常量
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<MarkerPunctuate>lambdaQuery().ne(MarkerPunctuate::getStatus, 0));
        List<Long> punctuateIdList = punctuatePage.getRecords().stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList());
        if (punctuateIdList.isEmpty()) {
            return new PageListVo<MarkerPunctuateVo>().setRecord(new ArrayList<>())
                    .setSize(punctuatePage.getSize())
                    .setTotal(punctuatePage.getTotal());
        }
        Map<Long, MarkerExtraPunctuate> extraPunctuateMap = markerExtraPunctuateMapper.selectList(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                        .in(MarkerExtraPunctuate::getPunctuateId, punctuateIdList))
                .stream().collect(Collectors.toMap(MarkerExtraPunctuate::getPunctuateId, extraPunctuate -> extraPunctuate));
        return new PageListVo<MarkerPunctuateVo>()
                .setRecord(punctuatePage.getRecords().parallelStream()
                        .map(punctuate -> new MarkerPunctuateDto(punctuate, extraPunctuateMap.get(punctuate.getPunctuateId()))
                                .getVo()).collect(Collectors.toList()))
                .setSize(punctuatePage.getSize())
                .setTotal(punctuatePage.getTotal());
    }

    /**
     * 分页查询自己提交的未通过的打点信息（不包含额外字段）
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点无额外字段的数据封装列表
     */
    @Override
    public PageListVo<MarkerSinglePunctuateVo> listSelfSinglePunctuatePage(PageSearchDto pageSearchDto, Long authorId) {
        Page<MarkerPunctuate> markerPunctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId));
        return new PageListVo<MarkerSinglePunctuateVo>()
                .setRecord(markerPunctuatePage.getRecords().parallelStream()
                        .map(MarkerSinglePunctuateDto::new)
                        .map(MarkerSinglePunctuateDto::getVo).collect(Collectors.toList()))
                .setSize(markerPunctuatePage.getSize())
                .setTotal(markerPunctuatePage.getTotal());
    }

    /**
     * 分页查询自己提交的未通过的打点信息（只包含额外字段）
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点只有额外字段的数据封装列表
     */
    @Override
    public PageListVo<MarkerExtraPunctuateVo> listSelfExtraPunctuatePage(PageSearchDto pageSearchDto, Long authorId) {
        Page<MarkerExtraPunctuate> extraPunctuatePage = markerExtraPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                .eq(MarkerExtraPunctuate::getAuthor, authorId));
        return new PageListVo<MarkerExtraPunctuateVo>()
                .setRecord(extraPunctuatePage.getRecords().stream()
                        .map(MarkerExtraPunctuateDto::new)
                        .map(MarkerExtraPunctuateDto::getVo).collect(Collectors.toList()))
                .setTotal(extraPunctuatePage.getTotal())
                .setSize(extraPunctuatePage.getSize());
    }

    /**
     * 提交暂存点位（不含额外字段）
     *
     * @param markerSinglePunctuateDto 打点无额外字段的数据封装
     * @return 打点ID
     */
    @Override
    public Long addSinglePunctuate(MarkerSinglePunctuateDto markerSinglePunctuateDto) {
        MarkerPunctuate markerPunctuate = markerSinglePunctuateDto.getEntity()
                //临时id
                .setPunctuateId(-1L)
                //TODO 将status做成常量
                .setStatus(0);
        //TODO 异常处理，第一次出错隔1+random(3)值重试
        markerPunctuateMapper.insert(markerPunctuate);
        //正式更新id
        markerPunctuateMapper.updateById(
                markerPunctuate.setPunctuateId(markerPunctuate.getId()));
        return markerPunctuate.getPunctuateId();
    }

    /**
     * 提交暂存点位额外字段
     *
     * @param markerExtraPunctuateDto 打点额外字段
     * @return 是否成功
     */
    @Override
    public Boolean addExtraPunctuate(MarkerExtraPunctuateDto markerExtraPunctuateDto) {
        MarkerExtraPunctuate extraPunctuateEntity = markerExtraPunctuateDto.getMarkerExtraEntity();
        if (0 == markerPunctuateMapper.selectCount(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, extraPunctuateEntity.getPunctuateId()))) {
            return false;
        }
        return markerExtraPunctuateMapper.insert(extraPunctuateEntity) == 1;
    }

    /**
     * 将暂存点位提交审核
     *
     * @param authorId 打点员ID
     * @return 是否成功
     */
    @Override
    public Boolean pushPunctuate(Long authorId) {
        markerPunctuateMapper.update(null, Wrappers.<MarkerPunctuate>lambdaUpdate()
                .eq(MarkerPunctuate::getAuthor, authorId)
                //TODO 将status做成常量
                .eq(MarkerPunctuate::getStatus, 0)
                .set(MarkerPunctuate::getStatus, 1));
        return true;
    }

    /**
     * 修改自身未提交的暂存点位（不包括额外字段）
     *
     * @param singlePunctuateDto 打点无额外字段的数据封装
     * @return 是否成功
     */
    @Override
    public Boolean updateSelfSinglePunctuate(MarkerSinglePunctuateDto singlePunctuateDto) {
        Long punctuateId = singlePunctuateDto.getPunctuateId();
        //旧打点信息
        MarkerPunctuate punctuate = markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                .and(wrapper -> wrapper
                        .eq(MarkerPunctuate::getStatus, 0)
                        .or()
                        .eq(MarkerPunctuate::getStatus, 2)));
        //赋予新信息对应的固有字段
        MarkerPunctuate newPunctuate = singlePunctuateDto.getEntity()
                .setStatus(0)
                .setAuditRemark(punctuate.getAuditRemark())
                .setId(punctuate.getId());
        return markerPunctuateMapper.updateById(newPunctuate) == 1;
    }

    /**
     * 修改自身未提交的暂存点位的额外字段
     *
     * @param extraPunctuateDto 打点额外字段
     * @return 是否成功
     */
    @Override
    public Boolean updateSelfPunctuateExtra(MarkerExtraPunctuateDto extraPunctuateDto) {
        Long punctuateId = extraPunctuateDto.getPunctuateId();
        //旧打点信息
        MarkerExtraPunctuate extraPunctuate = markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                .eq(MarkerExtraPunctuate::getPunctuateId, punctuateId)
                .and(wrapper -> wrapper
                        .eq(MarkerExtraPunctuate::getStatus, 0)
                        .or()
                        .eq(MarkerExtraPunctuate::getStatus, 2)));
        //赋予新信息对应的固有字段
        MarkerExtraPunctuate newExtraPunctuate = extraPunctuateDto.getMarkerExtraEntity()
                .setStatus(0)
                .setAuditRemark(extraPunctuate.getAuditRemark())
                .setId(extraPunctuate.getId());
        return markerExtraPunctuateMapper.updateById(newExtraPunctuate) == 1;
    }

    /**
     * 删除自己未通过的提交点位
     *
     * @param punctuateIdList 打点ID列表
     * @param authorId        打点员ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteSelfPunctuate(List<Long> punctuateIdList, Long authorId) {
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId)
                .in(MarkerPunctuate::getPunctuateId, punctuateIdList));
        markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                .eq(MarkerExtraPunctuate::getAuthor, authorId)
                .in(MarkerExtraPunctuate::getPunctuateId, punctuateIdList));
        return true;
    }

    private List<Long> getAllRelateIds(Long markerPunctuateId) {
        MarkerExtraPunctuate markerExtraPunctuate = Optional.ofNullable(markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, markerPunctuateId)))
                .orElse(new MarkerExtraPunctuate());
        if (!Boolean.TRUE.equals(markerExtraPunctuate.getIsRelated()))
            return Collections.singletonList(markerPunctuateId);
        return getAllRelateIds(markerPunctuateId, markerExtraPunctuate);
    }

    private List<Long> getAllRelateIds(Long markerPunctuateId, MarkerExtraPunctuate markerExtraPunctuate) {
        List<Long> allPunctuateIdList = new ArrayList<>();
        JSONObject extraContent = JSON.parseObject(markerExtraPunctuate.getMarkerExtraContent());
        //根据relate_type来判断生成allPunctuateIdList
        switch (extraContent.getString("relate_type")) {
            case "son_need_all":
            case "son_need_one":
                Long parentId = markerExtraPunctuate.getParentId();
                //allPunctuateIdList.add(parentId);
                String parentExtraContent = markerExtraPunctuateMapper.selectOne(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, parentId))
                        .getMarkerExtraContent();
                allPunctuateIdList.addAll(JSON.parseObject(parentExtraContent).getJSONArray("relate_list").toJavaList(Long.class));
                break;
            case "parent":
                allPunctuateIdList.add(markerPunctuateId);
            case "random_one":
                allPunctuateIdList.addAll(extraContent.getJSONArray("relate_list").toJavaList(Long.class));
                break;
        }
        return allPunctuateIdList;
    }
}
