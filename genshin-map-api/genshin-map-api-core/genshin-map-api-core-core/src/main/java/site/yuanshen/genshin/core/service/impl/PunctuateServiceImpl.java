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
import site.yuanshen.data.dto.MarkerExtraPunctuateDto;
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.MarkerSinglePunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.MarkerExtraPunctuate;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.mapper.MarkerExtraPunctuateMapper;
import site.yuanshen.data.mapper.MarkerPunctuateMapper;
import site.yuanshen.data.vo.MarkerExtraPunctuateVo;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.MarkerSinglePunctuateVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.PunctuateService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 打点服务接口实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PunctuateServiceImpl implements PunctuateService {

    private final MarkerPunctuateMapper markerPunctuateMapper;
    private final MarkerExtraPunctuateMapper markerExtraPunctuateMapper;

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    @Override
    @Cacheable("listPunctuatePage")
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
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
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
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
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
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
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
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
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
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
                    @CacheEvict(value = "listPunctuatePage", allEntries = true),
            }
    )
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
     * @param punctuateId 打点ID
     * @param authorId    打点员ID
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
    public Boolean deleteSelfPunctuate(Long punctuateId, Long authorId) {
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId)
                .eq(MarkerPunctuate::getPunctuateId, punctuateId));
        markerExtraPunctuateMapper.delete(Wrappers.<MarkerExtraPunctuate>lambdaQuery()
                .eq(MarkerExtraPunctuate::getAuthor, authorId)
                .eq(MarkerExtraPunctuate::getPunctuateId, punctuateId));
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
