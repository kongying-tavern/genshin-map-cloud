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
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.enums.PunctuateMethodEnum;
import site.yuanshen.data.enums.PunctuateStatusEnum;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.mapper.MarkerPunctuateMapper;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.PunctuateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private final MarkerMapper markerMapper;

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    @Override
    @Cacheable("listPunctuatePage")
    public PageListVo<MarkerPunctuateVo> listPunctuatePage(PageSearchDto pageSearchDto) {
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(),
                Wrappers.<MarkerPunctuate>lambdaQuery()
                        .eq(MarkerPunctuate::getStatus, PunctuateStatusEnum.COMMIT));
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
     * 分页查询自己提交的未通过的打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点无额外字段的数据封装列表
     */
    @Override
    public PageListVo<MarkerPunctuateVo> listSelfPunctuatePage(PageSearchDto pageSearchDto, Long authorId) {
        Page<MarkerPunctuate> markerPunctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId));
        return new PageListVo<MarkerPunctuateVo>()
                .setRecord(markerPunctuatePage.getRecords()
                        .parallelStream()
                        .map(MarkerPunctuateDto::new)
                        .map(MarkerPunctuateDto::getVo)
                        .collect(Collectors.toList()))
                .setSize(markerPunctuatePage.getSize())
                .setTotal(markerPunctuatePage.getTotal());
    }

    /**
     * 提交暂存点位
     *
     * @param punctuateDto 打点的数据封装
     * @return 打点ID
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
            }
    )
    public Long addPunctuate(MarkerPunctuateDto punctuateDto) {
        //保留原点位的初始标记者
        if (punctuateDto.getOriginalMarkerId() != null) {
            Marker marker = markerMapper.selectById(punctuateDto.getOriginalMarkerId());
            punctuateDto.setMarkerCreatorId(marker.getMarkerCreatorId());
        }
        MarkerPunctuate markerPunctuate = punctuateDto.getEntity()
                //临时id
                .setPunctuateId(-1L)
                .setStatus(0)
                //校验并设置打点操作类型
                .setMethodType(PunctuateMethodEnum.from(punctuateDto.getMethodType()).getTypeCode());
        markerPunctuateMapper.insert(markerPunctuate);
        //正式更新id
        markerPunctuateMapper.updateById(
                markerPunctuate.setPunctuateId(markerPunctuate.getId()));
        return markerPunctuate.getPunctuateId();
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
                .eq(MarkerPunctuate::getStatus, PunctuateStatusEnum.STAGE)
                .set(MarkerPunctuate::getStatus, PunctuateStatusEnum.STAGE));
        return true;
    }

    /**
     * 修改自身未提交的暂存点位
     *
     * @param punctuateDto 打点无额外字段的数据封装
     * @return 是否成功
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "searchPunctuateId", allEntries = true),
                    @CacheEvict(value = "listPunctuateById", allEntries = true),
                    @CacheEvict(value = "listAllPunctuatePage", allEntries = true),
            }
    )
    public Boolean updateSelfPunctuate(MarkerPunctuateDto punctuateDto) {
        Long punctuateId = punctuateDto.getPunctuateId();
        //旧打点信息
        MarkerPunctuate punctuate = markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                .and(wrapper -> wrapper
                        .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT))));
        //赋予新信息对应的固有字段
        MarkerPunctuate newPunctuate = punctuateDto.getEntity()
                .setMarkerCreatorId(punctuate.getOriginalMarkerId())
                .setStatus(0)
                .setAuditRemark(punctuate.getAuditRemark())
                .setId(punctuate.getId())
                //校验并设置打点操作类型
                .setMethodType(PunctuateMethodEnum.from(punctuateDto.getMethodType()).getTypeCode());
        return markerPunctuateMapper.updateById(newPunctuate) == 1;
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
            }
    )
    public Boolean deleteSelfPunctuate(Long punctuateId, Long authorId) {
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId)
                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT)));
        return true;
    }

}
