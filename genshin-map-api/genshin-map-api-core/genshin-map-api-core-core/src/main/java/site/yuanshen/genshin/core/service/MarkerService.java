package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerItemLinkDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.helper.marker.tweak.MarkerTweakDataHelper;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.mbp.HistoryMBPService;
import site.yuanshen.genshin.core.service.mbp.MarkerItemLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.MarkerMBPService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 点位服务接口实现
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerService {

    private final MarkerMapper markerMapper;
    private final MarkerMBPService markerMBPService;
    private final MarkerDao markerDao;
    private final MarkerLinkageDao markerLinkageDao;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerItemLinkMBPService markerItemLinkMBPService;
    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final HistoryMBPService historyMBPService;

    /**
     * 根据各种条件筛选查询点位ID
     *
     * @param searchVo 点位查询前端封装
     * @return 点位ID列表
     */
    @Cacheable(value = "searchMarkerId")
    public List<Long> searchMarkerId(MarkerSearchVo searchVo, List<Integer> hiddenFlagList) {
        boolean isArea = !(searchVo.getAreaIdList() == null || searchVo.getAreaIdList().isEmpty());
        boolean isItem = !(searchVo.getItemIdList() == null || searchVo.getItemIdList().isEmpty());
        boolean isType = !(searchVo.getTypeIdList() == null || searchVo.getTypeIdList().isEmpty());
        if (isArea && isItem || isArea && isType || isType && isItem)
            throw new GenshinApiException("条件冲突");
        List<Long> itemIdList = new ArrayList<>();
        if (isArea) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getAreaId, searchVo.getAreaIdList()).in(Item::getHiddenFlag, hiddenFlagList)
                            .select(Item::getId))
                    .stream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }
        if (isItem) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getId, searchVo.getItemIdList()).in(Item::getHiddenFlag, hiddenFlagList)
                            .select(Item::getId)).stream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }
        if (isType) {
            List<Long> tempItemIdList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                            .in(ItemTypeLink::getTypeId, searchVo.getTypeIdList())
                            .select(ItemTypeLink::getItemId))
                    .stream()
                    .map(ItemTypeLink::getItemId).distinct().collect(Collectors.toList());
            if (tempItemIdList.isEmpty()) return new ArrayList<>();
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getId, tempItemIdList).in(Item::getHiddenFlag, hiddenFlagList)
                            .select(Item::getId)).stream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }


            log.info("从物品ID获取正式点位ID， 物品ID:{}", itemIdList);
            if (itemIdList.isEmpty()) return new ArrayList<>();
            return markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
                            .in(MarkerItemLink::getItemId, itemIdList)
                            .select(MarkerItemLink::getMarkerId))
                    .stream()
                    .map(MarkerItemLink::getMarkerId)
                    .distinct().collect(Collectors.toList());
    }

    /**
     * 根据各种条件查询所有点位信息
     *
     * @param markerSearchVo 点位查询前端封装
     * @return 点位完整信息的数据封装列表
     */
    //此处是两个方法的缝合，不需要加缓存
    public List<MarkerVo> searchMarker(MarkerSearchVo markerSearchVo, List<Integer> hiddenFlagList) {
        final MarkerService markerService = (MarkerService) SpringContextUtils.getBean("markerService");
        List<Long> markerIdList = markerService.searchMarkerId(markerSearchVo, hiddenFlagList);
        List<MarkerVo> result = markerService.listMarkerById(markerIdList, hiddenFlagList);
        return result;
    }


    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList 点位ID列表
     * @return 点位完整信息的数据封装列表
     */
    public List<MarkerVo> listMarkerById(List<Long> markerIdList, List<Integer> hiddenFlagList) {
        //为空直接返回
        if (markerIdList.isEmpty()) return new ArrayList<>();
        List<MarkerVo> result = markerDao.listMarkerById(markerIdList, hiddenFlagList);
        return result;
    }


    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param hiddenFlagList   hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto,List<Integer> hiddenFlagList) {
        PageListVo<MarkerVo> page = markerDao.listMarkerPage(pageSearchDto, hiddenFlagList);
        return page;
    }

    /**
     * 新增点位（不包括额外字段）
     *
     * @param markerDto 点位无Extra的数据封装
     * @return 新点位ID
     */
    @Transactional
    public Long createMarker(MarkerDto markerDto) {
        Marker marker = markerDto.getEntity();
        markerMapper.insert(marker);
        List<MarkerItemLink> itemLinkList = new ArrayList<>(
                markerDto.getItemList().parallelStream()
                        .map(MarkerItemLinkDto::new).map(dto -> dto.withMarkerId(marker.getId()).getEntity())
                        .collect(Collectors.toMap(MarkerItemLink::getItemId, Function.identity())).values());
        markerItemLinkMBPService.saveBatch(itemLinkList);

        return marker.getId();
    }

    /**
     * 修改点位（不包括额外字段）
     *
     * @param markerDto 点位无Extra的数据封装
     * @return 是否成功
     */
    @Transactional
    public Boolean updateMarker(MarkerDto markerDto) {
        //查询修改前的记录
        MarkerDto markerRecord = buildMarkerDto(markerDto.getId());

        //将当前记录保存为历史记录
        this.saveHistory(markerRecord, HistoryEditType.UPDATE);

        Map<String, Object> mergeResult = JsonUtils.merge(markerRecord.getExtra(), markerDto.getExtra());
        markerDto.setExtra(mergeResult);

        Boolean updated = this.saveMarker(markerDto);
        if(!updated) {
            throw new GenshinApiException("该点位已更新，请重新提交");
        }
        return updated;
    }

    /**
     * 根据点位ID删除点位
     *
     * @param markerId 点位ID列表
     * @return 是否成功
     */
    @Transactional
    public Boolean deleteMarker(Long markerId) {
        //查询修改前的记录
        MarkerDto markerRecord = buildMarkerDto(markerId);
        //将当前记录保存为历史记录
        this.saveHistory(markerRecord, HistoryEditType.DELETE);

        markerMapper.delete(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, markerId));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerId));
        markerLinkageDao.removeRelatedLinkageList(Collections.singletonList(markerId), true);
        return true;
    }

    /**
     * 调整点位数据
     *
     * @param tweakVo 点位调整配置
     * @return 修改后的点位数据
     */
    @Transactional
    public List<MarkerVo> tweakMarkers(TweakVo tweakVo) {
        List<Long> markerIds = tweakVo.getMarkerIds();
        if(CollUtil.isEmpty(markerIds)) {
            return new ArrayList<>();
        }

        // 生成数据
        final List<MarkerDto> originalMarkers = this.buildMarkerDto(markerIds);
        final List<MarkerDto> tweakedMarkers = originalMarkers.parallelStream()
                .map(MarkerDto::getCopy)
                .filter(Objects::nonNull)
                .map(marker -> MarkerTweakDataHelper.applyTweakRules(marker, tweakVo.getTweaks()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(CollUtil.isEmpty(tweakedMarkers)) {
            return new ArrayList<>();
        }

        // 保存数据
        this.saveMarker(tweakedMarkers, tweakedMarkers.size());
        this.saveHistory(originalMarkers, HistoryEditType.UPDATE);

        // 重新获取数据，防止返回旧数据
        List<MarkerDto> updatedMarkers = this.buildMarkerDto(markerIds);
        return updatedMarkers.parallelStream()
                .filter(Objects::nonNull)
                .map(MarkerDto::getVo)
                .collect(Collectors.toList());
    }

    //--------------------构造点位Dto-----------------------
    private MarkerDto buildMarkerDto(Long markerId) {
        final List<MarkerDto> markerList = buildMarkerDto(List.of(markerId));
        return markerList.isEmpty() ? null : markerList.get(0);
    }

    private List<MarkerDto> buildMarkerDto(List<Long> markerId) {
        final String markerIdListStr = PgsqlUtils.unnestLongStr(markerId);
        final List<Marker> markerList = markerMapper.selectListWithLargeIn(markerIdListStr, Wrappers.<Marker>lambdaQuery());
        final List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectWithLargeCustomIn("marker_id", markerIdListStr, Wrappers.<MarkerItemLink>lambdaQuery());

        final Map<Long, List<MarkerItemLinkDto>> markerItemLinkGroup = markerItemLinkList.parallelStream()
                .map(MarkerItemLinkDto::new)
                .collect(Collectors.groupingBy(MarkerItemLinkDto::getMarkerId));

        return markerList.stream()
                .map(MarkerDto::new)
                .map(marker -> marker.withItemList(
                        markerItemLinkGroup
                                .getOrDefault(marker.getId(), List.of())
                                .stream()
                                .map(MarkerItemLinkDto::getVo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    //--------------------点位相关辅助方法----------------------
    private boolean saveMarker(MarkerDto markerDto) {
        return saveMarker(List.of(markerDto), 1);
    }

    private boolean saveMarker(List<MarkerDto> markerDtos, int validateCount) {
        // Extract data
        final List<Marker> markerList = markerDtos.parallelStream()
                .map(MarkerDto::getEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<Long> markerIdList = markerList.parallelStream()
                .map(Marker::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        final List<MarkerItemLink> itemList = markerDtos.parallelStream()
                .map(markerDto -> {
                    List<MarkerItemLinkVo> itemListData = markerDto.getItemList();
                    if (itemListData == null) {
                        return null;
                    }
                    return itemListData.parallelStream()
                            .map(MarkerItemLinkDto::new)
                            .map(dto -> dto.withMarkerId(markerDto.getId()).getEntity())
                            .collect(Collectors.toList());
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<Long> itemMarkerIdList = itemList.parallelStream()
                .map(MarkerItemLink::getMarkerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Save marker
        if(CollUtil.isEmpty(markerIdList)) {
            return validateCount == 0;
        }
        boolean updated = markerMBPService.saveOrUpdateBatch(markerList, 100);
        if(!updated) {
            return false;
        }
        if(CollUtil.isNotEmpty(itemMarkerIdList)) {
            markerItemLinkMapper.deleteWithLargeCustomIn("marker_id", PgsqlUtils.unnestLongStr(itemMarkerIdList), Wrappers.<MarkerItemLink>lambdaQuery());
        }
        if(CollUtil.isNotEmpty(itemList)) {
            markerItemLinkMBPService.saveBatch(itemList);
        }
        return true;
    }

    //--------------------保存历史信息----------------------
    private void saveHistory(MarkerDto markerDto, HistoryEditType historyType) {
        saveHistory(List.of(markerDto), historyType);
    }

    private void saveHistory(List<MarkerDto> markerDtos, HistoryEditType historyType) {
        if(CollUtil.isEmpty(markerDtos)) {
            return;
        }

        List<History> historyList = markerDtos.stream()
                .filter(Objects::nonNull)
                .map(markerRecord -> HistoryConvert.convert(markerRecord, historyType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        historyMBPService.saveBatch(historyList, 100);
    }
}
