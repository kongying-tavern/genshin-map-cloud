package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerItemLinkDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.service.MarkerService;
import site.yuanshen.genshin.core.service.mbp.MarkerItemLinkMBPService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final MarkerDao markerDao;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerItemLinkMBPService markerItemLinkMBPService;
    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final HistoryMapper historyMapper;

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
            throw new RuntimeException("条件冲突");
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
                            .in(Item::getId, itemIdList).in(Item::getHiddenFlag, hiddenFlagList)
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
        List<Long> markerIdList = searchMarkerId(markerSearchVo, hiddenFlagList);
        List<MarkerVo> result = listMarkerById(markerIdList, hiddenFlagList);
        UserAppenderService.appendUser(result, MarkerVo::getCreatorId, MarkerVo::getCreatorId, MarkerVo::setCreator);
        UserAppenderService.appendUser(result, MarkerVo::getUpdaterId, MarkerVo::getUpdaterId, MarkerVo::setUpdater);
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
        UserAppenderService.appendUser(result, MarkerVo::getCreatorId, MarkerVo::getCreatorId, MarkerVo::setCreator);
        UserAppenderService.appendUser(result, MarkerVo::getUpdaterId, MarkerVo::getUpdaterId, MarkerVo::setUpdater);
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
        List<MarkerVo> result = page.getRecord();
        UserAppenderService.appendUser(result, MarkerVo::getCreatorId, MarkerVo::getCreatorId, MarkerVo::setCreator);
        UserAppenderService.appendUser(result, MarkerVo::getUpdaterId, MarkerVo::getUpdaterId, MarkerVo::setUpdater);
        page.setRecord(result);
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
        MarkerDto markerRecord = buildMarkerDto(markerDto.getId());
        //保存历史记录
        saveHistoryMarker(markerRecord);

        Map<String, Object> mergeResult = JsonUtils.merge(markerRecord.getExtra(), markerDto.getExtra());
        markerDto.setExtra(mergeResult);

        Boolean updated = markerMapper.update(markerDto.getEntity(), Wrappers.<Marker>lambdaUpdate()
                .eq(Marker::getId, markerDto.getId())) == 1;
        if (!updated) {
            throw new OptimisticLockingFailureException("该点位已更新，请重新提交");
        }

        if (markerDto.getItemList() != null && !markerDto.getItemList().isEmpty()) {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerDto.getId()));
            List<MarkerItemLink> itemLinkList = markerDto.getItemList().parallelStream().map(MarkerItemLinkDto::new).map(dto->dto.withMarkerId(markerDto.getId()).getEntity()).collect(Collectors.toList());
            markerItemLinkMBPService.saveBatch(itemLinkList);
        } else if (markerDto.getItemList() != null) {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerDto.getId()));
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
        markerMapper.delete(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, markerId));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerId));
        return true;
    }

    //--------------------储存历史信息-----------------------

    private MarkerDto buildMarkerDto(Long markerId) {
        Marker marker = markerMapper.selectOne(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, markerId));
        return new MarkerDto(marker).withItemList(
                markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerId))
                        .stream()
                        .map(MarkerItemLinkDto::new)
                        .map(MarkerItemLinkDto::getVo)
                        .collect(Collectors.toList()));
    }

    private void saveHistoryMarker(MarkerDto dto) {
        History history = HistoryConvert.convert(dto);
        //存储入库
        historyMapper.insert(history);
    }

}
