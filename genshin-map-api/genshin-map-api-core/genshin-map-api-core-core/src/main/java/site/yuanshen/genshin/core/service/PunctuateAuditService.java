package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.enums.PunctuateStatusEnum;
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
public class PunctuateAuditService {

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
                            .in(MarkerPunctuate::getAuthor, searchVo.getAuthorList()))
                    .stream().map(MarkerPunctuate::getPunctuateId).collect(Collectors.toList());
        }
        List<Long> result = new ArrayList<>();
        itemIdList.parallelStream().forEach(itemId ->
                result.addAll(markerPunctuateMapper.selectList(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .in(isAuthor, MarkerPunctuate::getAuthor, searchVo.getAuthorList())
                                //需要注意库中究竟存了什么
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
    public PageListVo<MarkerPunctuateVo> listAllPunctuatePage(PageSearchDto pageSearchDto) {
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.lambdaQuery());
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
    @Transactional
    @CacheEvict(value = "listPunctuatePage", allEntries = true)
    public Long passPunctuate(Long punctuateId) {
        //打点信息
        MarkerPunctuate markerPunctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                                .eq(MarkerPunctuate::getStatus, PunctuateStatusEnum.COMMIT.getValue())))
                .orElseThrow(() -> new RuntimeException("无打点相关信息，请联系系统管理员"));
        switch (markerPunctuate.getMethodType()) {
            case ADD: {
                //插入自身
                Marker marker = BeanUtils.copy(markerPunctuate, Marker.class)
                        //ID应为空
                        .withId(null);
                markerMapper.insert(marker);
                //清除提交信息
                markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
                return marker.getId();
            }
            case UPDATE: {
                //获取原有点位id
                Long originalMarkerId = Optional.ofNullable(markerPunctuate.getOriginalMarkerId())
                        .orElseThrow(() -> new RuntimeException("无法找到修改点位的原始id，请联系管理员"));
                //根据ID查询原有点位
                Marker oldMarker = Optional.ofNullable(markerMapper.selectOne(Wrappers.<Marker>lambdaQuery().eq(Marker::getId, originalMarkerId)))
                        .orElseThrow(() -> new RuntimeException("无法找到原始id对应的原始点位，无法做出更改，请联系管理员"));
                //原有点位拷贝一份作为新点位
                Marker newMarker = BeanUtils.copy(oldMarker, Marker.class);
                //打点的更改信息复制到新点位中（使用了hutool的copy，忽略null值）
                BeanUtils.copyNotNull(markerPunctuate, newMarker);
                markerMapper.updateById(newMarker);
                return newMarker.getId();
            }
            case DELETE: {
                //获取原有点位id
                Long originalMarkerId = Optional.ofNullable(markerPunctuate.getOriginalMarkerId())
                        .orElseThrow(() -> new RuntimeException("无法找到修改点位的原始id，请联系管理员"));
                //删除
                markerMapper.deleteById(originalMarkerId);
                //清除提交信息
                markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
                return originalMarkerId;
            }
            default:
                throw new RuntimeException("这是一条不可能的报错，如果你看到了这条报错，请立刻联系开发者");
        }
    }

    /**
     * 驳回点位审核
     *
     * @param punctuateId 打点ID
     * @param auditRemark 审核备注
     * @return 是否成功
     */
    @Transactional
    @CacheEvict(value = "listPunctuatePage", allEntries = true)
    public Boolean rejectPunctuate(Long punctuateId, String auditRemark) {
        //打点信息
        MarkerPunctuate markerPunctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                                .eq(MarkerPunctuate::getStatus, PunctuateStatusEnum.COMMIT.getValue())))
                .orElseThrow(() -> new RuntimeException("无打点相关信息，请联系系统管理员"));
        markerPunctuateMapper.updateById(markerPunctuate.withStatus(PunctuateStatusEnum.REJECT).withAuditRemark(auditRemark));
        return true;
    }

    /**
     * 删除提交点位
     *
     * @param punctuateId 打点ID
     * @return 是否成功
     */
    @Transactional
    @CacheEvict(value = "listPunctuatePage", allEntries = true)
    public Boolean deletePunctuate(Long punctuateId) {
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery().eq(MarkerPunctuate::getPunctuateId, punctuateId));
        return true;
    }

}
