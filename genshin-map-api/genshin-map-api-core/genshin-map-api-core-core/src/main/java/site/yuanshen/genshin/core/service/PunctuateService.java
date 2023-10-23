package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.enums.PunctuateStatusEnum;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.mapper.MarkerPunctuateMapper;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.PunctuateService;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 打点服务接口实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PunctuateService {

    private final MarkerPunctuateMapper markerPunctuateMapper;

    private final MarkerMapper markerMapper;

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    @Cacheable("listPunctuatePage")
    public PageListVo<MarkerPunctuateVo> listPunctuatePage(PageSearchDto pageSearchDto) {
        Page<MarkerPunctuate> punctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(),
                Wrappers.<MarkerPunctuate>lambdaQuery()
                        .eq(MarkerPunctuate::getStatus, PunctuateStatusEnum.COMMIT));
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
     * 分页查询自己提交的提交的打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点无额外字段的数据封装列表
     */
    public PageListVo<MarkerPunctuateVo> listSelfPunctuatePage(PageSearchDto pageSearchDto, Long authorId) {
        Page<MarkerPunctuate> markerPunctuatePage = markerPunctuateMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<MarkerPunctuate>lambdaQuery()
                        .eq(MarkerPunctuate::getAuthor, authorId)
                .in(MarkerPunctuate::getStatus,Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT)));
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
    @Transactional
    public Long addPunctuate(MarkerPunctuateDto punctuateDto) {
        //保留原点位的初始标记者
        if (punctuateDto.getOriginalMarkerId() != null) {
            Marker marker = markerMapper.selectById(punctuateDto.getOriginalMarkerId());
            punctuateDto.setMarkerCreatorId(marker.getMarkerCreatorId());
        }
        MarkerPunctuate markerPunctuate = punctuateDto.getEntity()
                //临时id
                .withPunctuateId(-1L)
                .withStatus(PunctuateStatusEnum.STAGE);
        markerPunctuateMapper.insert(markerPunctuate);
        //正式更新id
        markerPunctuateMapper.updateById(
                markerPunctuate.withPunctuateId(markerPunctuate.getId()));
        return markerPunctuate.getPunctuateId();
    }

    /**
     * 将暂存点位提交审核
     *
     * @param authorId 打点员ID
     * @return 是否成功
     */
    @Transactional
    @CacheEvict(value = "listPunctuatePage", allEntries = true)
    public Boolean pushPunctuate(Long authorId) {
        if (markerPunctuateMapper.selectCount(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getAuthor, authorId)
                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT)))
                <= 0)
            throw new GenshinApiException("无可提交点位");
        markerPunctuateMapper.update(null, Wrappers.<MarkerPunctuate>lambdaUpdate()
                .eq(MarkerPunctuate::getAuthor, authorId)
                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT))
                .set(MarkerPunctuate::getStatus, PunctuateStatusEnum.COMMIT));
        return true;
    }

    /**
     * 修改自身未提交的暂存点位
     *
     * @param punctuateDto 打点无额外字段的数据封装
     * @return 是否成功
     */
    @Transactional
    public Boolean updateSelfPunctuate(MarkerPunctuateDto punctuateDto) {
        //打点信息
        MarkerPunctuate punctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .eq(MarkerPunctuate::getPunctuateId, punctuateDto.getPunctuateId())
                                .eq(MarkerPunctuate::getAuthor, punctuateDto.getAuthor())
                                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT))))
                .orElseThrow(() -> new RuntimeException("无该打点信息，请联系管理员"));
        //赋予新信息对应的固有字段
        MarkerPunctuate newPunctuate = punctuateDto.getEntity()
                .withMarkerCreatorId(punctuate.getOriginalMarkerId())
                .withStatus(PunctuateStatusEnum.STAGE)
                .withAuditRemark(punctuate.getAuditRemark())
                .withId(punctuate.getId());
        return markerPunctuateMapper.updateById(newPunctuate) == 1;
    }

    /**
     * 删除自己未提交的提交点位
     *
     * @param punctuateId 打点ID
     * @param authorId    打点员ID
     * @return 是否成功
     */
    @Transactional
    public Boolean deleteSelfPunctuate(Long punctuateId, Long authorId) {
        //打点信息
        MarkerPunctuate punctuate = Optional.ofNullable(
                        markerPunctuateMapper.selectOne(Wrappers.<MarkerPunctuate>lambdaQuery()
                                .eq(MarkerPunctuate::getAuthor, authorId)
                                .eq(MarkerPunctuate::getPunctuateId, punctuateId)
                                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT))))
                .orElseThrow(() -> new RuntimeException("无该打点信息，请联系管理员"));
        markerPunctuateMapper.delete(Wrappers.<MarkerPunctuate>lambdaQuery()
                .eq(MarkerPunctuate::getPunctuateId, punctuate.getPunctuateId())
                .in(MarkerPunctuate::getStatus, Arrays.asList(PunctuateStatusEnum.STAGE, PunctuateStatusEnum.REJECT)));
        return true;
    }

}
