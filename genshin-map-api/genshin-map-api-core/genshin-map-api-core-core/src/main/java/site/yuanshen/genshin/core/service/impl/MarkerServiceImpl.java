package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.web.utils.JsonUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerExtraDto;
import site.yuanshen.data.dto.MarkerSingleDto;
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
import java.util.stream.Collectors;

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
    private final MarkerDao markerDao;
    private final MarkerExtraMapper markerExtraMapper;
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
    @Override
    @Cacheable(value = "searchMarkerId")
    public List<Long> searchMarkerId(MarkerSearchVo searchVo) {
        boolean isArea = !(searchVo.getAreaIdList() == null || searchVo.getAreaIdList().isEmpty());
        boolean isItem = !(searchVo.getItemIdList() == null || searchVo.getItemIdList().isEmpty());
        boolean isType = !(searchVo.getTypeIdList() == null || searchVo.getTypeIdList().isEmpty());
        if (isArea && (isItem || isType) || isItem && isType)
            throw new RuntimeException("条件冲突");
        List<Long> itemIdList = new ArrayList<>();
        if (isArea) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(Item::getAreaId, searchVo.getAreaIdList()).in(!searchVo.getHiddenFlagList().isEmpty(), Item::getHiddenFlag, searchVo.getHiddenFlagList())
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

        //如果不是按地区筛选,也就是说没经过筛选内鬼这一步,则再筛一遍 TODO:感觉繁琐了
        if (!isArea) {
            itemIdList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                            .in(!itemIdList.isEmpty(),Item::getId, itemIdList).in(!searchVo.getHiddenFlagList().isEmpty(),Item::getHiddenFlag, searchVo.getHiddenFlagList())
                            .select(Item::getId)).stream()
                    .map(Item::getId).distinct().collect(Collectors.toList());
        }


        if (!searchVo.getGetBeta()) {
            log.info("获取正式点位:{}", itemIdList.subList(0, itemIdList.size()>50?50:itemIdList.size()));
            if (itemIdList.isEmpty()) return new ArrayList<>();
            return markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
                            .in(MarkerItemLink::getItemId, itemIdList)
                            .select(MarkerItemLink::getMarkerId))
                    .stream()
                    .map(MarkerItemLink::getMarkerId)
                    .distinct().collect(Collectors.toList());
        } else {
            log.info("获取测试点位:{}", itemIdList);
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
     * 通过ID列表查询点位信息
     *
     * @param markerIdList 点位ID列表
     * @return 点位完整信息的数据封装列表
     */
    @Override
    @Cacheable(value = "listMarkerById")
    public List<MarkerDto> listMarkerById(List<Long> markerIdList, List<Integer> hiddenFlagList) {
        //为空直接返回
        if (markerIdList.isEmpty()) return new ArrayList<>();
        //获取所有的额外字段
        List<Long> list = new ArrayList<>();
        for (Long aLong : markerIdList) {
            list.add(aLong);
        }
        Map<Long, MarkerExtra> markerExtraMap = markerExtraMapper.selectList(Wrappers.<MarkerExtra>lambdaQuery().apply("marker_id = any({0}::bigint[])",  markerIdList.toString().replace('[','{').replace(']','}')))
                    .stream().collect(Collectors.toMap(MarkerExtra::getMarkerId, markerExtra -> markerExtra));
        //获取关联的物品Id
        Map<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();

        List<MarkerItemLink> markerItemLinks = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().apply("marker_id = any({0}::bigint[])", markerIdList.toString().replace('[','{').replace(']','}')));
        //获取item_id,得到item合集
        Map<Long, Item> itemMap = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(!markerItemLinks.isEmpty(),Item::getId, markerItemLinks.stream().map(MarkerItemLink::getItemId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Item::getId, Item -> Item));

        markerItemLinks.parallelStream().forEach(markerItemLink ->
                itemLinkMap.compute(markerItemLink.getMarkerId(),
                        (markerId, linkList) -> {
                            if (linkList == null) return new ArrayList<>(Collections.singletonList(markerItemLink));
                            linkList.add(markerItemLink);
                            return linkList;
                        })
        );
        //构建返回
        return markerMapper.selectList(Wrappers.<Marker>lambdaQuery().apply("id = any({0}::bigint[])", markerIdList.toString().replace('[','{').replace(']','}')).in(!hiddenFlagList.isEmpty(), Marker::getHiddenFlag, hiddenFlagList))
                .parallelStream().map(marker ->
                        new MarkerDto(marker,
                                markerExtraMap.get(marker.getId()),
                                itemLinkMap.get(marker.getId())
                                , itemMap)).collect(Collectors.toList());
    }


    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param hiddenFlagList   hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    @Override
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto,List<Integer> hiddenFlagList) {
        return markerDao.listMarkerPage(pageSearchDto,hiddenFlagList);
    }

    /**
     * 新增点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 新点位ID
     */
    @Override
    @Transactional
    public Long createMarker(MarkerSingleDto markerSingleDto) {
        Marker marker = markerSingleDto.getEntity();
        markerMapper.insert(marker);
        //正式更新id item_id+marker_id得唯一
        List<MarkerItemLink> itemLinkList = markerSingleDto.getItemList().parallelStream().map(markerItemLinkDto -> markerItemLinkDto.getEntity().setMarkerId(marker.getId())).collect(
                Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getItemId() + ";" + o.getMarkerId()))), ArrayList::new));
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
    @Transactional
    public Boolean addMarkerExtra(MarkerExtraDto markerExtraDto) {
        boolean added = markerExtraMapper.insert(markerExtraDto.getEntity()) == 1;


        return added;
    }

    /**
     * 修改点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean updateMarker(MarkerSingleDto markerSingleDto) {
        //保存历史记录
        MarkerExtra markerExtra = markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, markerSingleDto.getId()));
        saveHistoryMarker(buildMarkerDto(markerSingleDto.getId(), markerExtra));


        Boolean updated = markerMapper.update(markerSingleDto.getEntity(), Wrappers.<Marker>lambdaUpdate()
                .eq(Marker::getId, markerSingleDto.getId())) == 1;
        if (!updated) {
            throw new OptimisticLockingFailureException("该点位已更新，请重新提交");
        }

        if (markerSingleDto.getItemList() != null && !markerSingleDto.getItemList().isEmpty()) {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerSingleDto.getId()));
            List<MarkerItemLink> itemLinkList = markerSingleDto.getItemList().parallelStream().map(markerItemLinkDto -> markerItemLinkDto.getEntity().setMarkerId(markerSingleDto.getId())).collect(Collectors.toList());
            markerItemLinkMBPService.saveBatch(itemLinkList);
        } else if (markerSingleDto.getItemList() != null) {
            markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerSingleDto.getId()));
        }
        return updated;
    }


    /**
     * 修改点位额外字段
     *
     * @param markerExtraDto 点位额外信息的数据封装
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean updateMarkerExtra(MarkerExtraDto markerExtraDto) {
        //TODO:如果增加乐观锁后,默认搜不到情况下version=0.第一个人先未找到,进行新增,此时version为默认值1.第二个人进行找到,0和1匹配不上,更新失败
        //2.情况2-两人一起到达搜索,此时都找不到,1号先完成了添加,2号再完成了添加,此时会有两条一模一样数据,但是搜索出来的时候只会使用一条,影响不大.
        MarkerExtra markerExtra = markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, markerExtraDto.getMarkerId()));

        saveHistoryMarker(buildMarkerDto(markerExtraDto.getMarkerId(), markerExtra));
        if (markerExtra == null) {
            return addMarkerExtra(markerExtraDto);
        }

        String mergeResult = JsonUtils.merge(markerExtra.getMarkerExtraContent(), markerExtraDto.getMarkerExtraContent());
        markerExtraDto.setMarkerExtraContent(mergeResult);

        boolean updated = markerExtraMapper.update(markerExtraDto.getEntity(), Wrappers.<MarkerExtra>lambdaUpdate()
                .eq(MarkerExtra::getMarkerId, markerExtraDto.getMarkerId())) == 1;

        return updated;
    }


    /**
     * 根据点位ID删除点位
     *
     * @param markerId 点位ID列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean deleteMarker(Long markerId) {
        markerMapper.delete(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, markerId));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerId));
        markerExtraMapper.delete(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, markerId));


        return true;
    }

    //--------------------储存历史信息-----------------------

    private MarkerDto buildMarkerDto(Long markerId, MarkerExtra markerExtra) {
        Marker marker = markerMapper.selectOne(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, markerId));
        //获取关联的物品ID
        List<MarkerItemLink> markerItemLinks = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getMarkerId, markerId));
        return new MarkerDto(marker, markerExtra, markerItemLinks);
    }

    private void saveHistoryMarker(MarkerDto dto) {
        History history = HistoryConvert.convert(dto);
        //存储入库
        historyMapper.insert(history);
    }

}
