package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import site.yuanshen.data.entity.*;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.PunctuateSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.PunctuateAuditService;
import site.yuanshen.genshin.core.service.mbp.MarkerExtraMBPService;

import java.util.*;
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
    private final MarkerExtraMapper markerExtraMapper;
    private final MarkerExtraMBPService markerExtraMBPService;
    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final MarkerExtraPunctuateMapper markerExtraPunctuateMapper;
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
    @Cacheable("listAllPunctuatePage")
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
            Marker newMarker = BeanUtils.copyProperties(oldMarker, Marker.class);
            //打点的更改信息复制到新点位中（使用了hutool的copy，忽略null值）
            BeanUtils.copyNotNull(markerPunctuate, newMarker);
            markerMapper.updateById(newMarker);
            //是否有额外字段
            if (markerExtraPunctuateOptional.isPresent()) {
                MarkerExtraPunctuate markerExtraPunctuate = markerExtraPunctuateOptional.get();
                //根据ID查询原有点位的额外信息
                MarkerExtra markerExtra = Optional.ofNullable(markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, originalMarkerId)))
                        .orElseThrow(() -> new RuntimeException("无原始的额外字段，请先对点位进行插入操作"));
                //打点的额外信息更改复制到点位中（使用了hutool的copy，忽略null值）
                BeanUtils.copyNotNull(markerExtraPunctuate, markerExtra);
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
                                            BeanUtils.copyNotNull(relateMarkerPunctuate, relateMarker);
                                            markerMapper.updateById(relateMarker);
                                            //关联点位有extra信息，更新
                                            if (relateMarkerExtraPunctuateOptional.isPresent()) {
                                                MarkerExtraPunctuate relateMarkerExtraPunctuate = relateMarkerExtraPunctuateOptional.get();
                                                //根据ID查询原有点位的额外信息
                                                MarkerExtra relateMarkerExtra = Optional.ofNullable(markerExtraMapper.selectOne(Wrappers.<MarkerExtra>lambdaQuery().eq(MarkerExtra::getMarkerId, relateOriginalMarkerId)))
                                                        .orElseThrow(() -> new RuntimeException("无原始的额外字段，请先对点位进行插入操作"));
                                                //打点的额外信息更改复制到点位中（使用了hutool的copy，忽略null值）
                                                BeanUtils.copyNotNull(relateMarkerExtraPunctuate, relateMarkerExtra);
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
            Marker marker = BeanUtils.copyProperties(markerPunctuate, Marker.class)
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
                        Marker relateMarker = BeanUtils.copyProperties(punctuate, Marker.class);
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
        markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery().eq(MarkerExtraPunctuate::getPunctuateId, punctuateId));
        return true;
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
