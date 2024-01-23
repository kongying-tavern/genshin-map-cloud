package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.enums.notice.NoticeTransformerEnum;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeMapper noticeMapper;

    @Cacheable(value = "listNotice")
    public PageListVo<NoticeVo> listNotice(NoticeSearchDto noticeSearchDto) {
        final Boolean isValid = noticeSearchDto.getGetValid();

        String channelArrStr = "";
        if(CollUtil.isNotEmpty(noticeSearchDto.getChannels())) {
            final List<String> channels = noticeSearchDto.getChannels();
            final List<String> channelArr = channels.stream().map(channel -> "'" + channel + "'").collect(Collectors.toList());
            channelArrStr = StrUtil.join(",", channelArr);
        }
        final Page<Notice> result = noticeMapper.selectPage(
            noticeSearchDto.getPageEntity().setOptimizeCountSql(false),
            Wrappers.<Notice>lambdaQuery()
                .apply(String.format("(channel::jsonb) ??| array[%s]", channelArrStr))
                .like(StrUtil.isNotBlank(noticeSearchDto.getTitle()), Notice::getTitle, noticeSearchDto.getTitle())
                .nested(isValid != null, cwValid -> {
                    final Timestamp ts = TimeUtils.getCurrentTimestamp();
                    if(isValid != null) {
                        cwValid
                                .nested(cwST -> {
                                    cwST
                                            .nested(cwSTN -> {cwSTN.isNull(Notice::getValidTimeStart);}).or()
                                            .nested(cwSTN -> {cwSTN.isNotNull(Notice::getValidTimeStart).le(Notice::getValidTimeStart, ts);});
                                })
                                .nested(cwET -> {
                                    cwET
                                            .nested(cwETN -> {cwETN.isNull(Notice::getValidTimeEnd);}).or()
                                            .nested(cwETN -> {cwETN.isNotNull(Notice::getValidTimeEnd).ge(Notice::getValidTimeEnd, ts);});
                                });
                    } else {
                        cwValid
                                .nested(cwST -> {
                                    cwST
                                            .isNotNull(Notice::getValidTimeStart)
                                            .gt(Notice::getValidTimeStart, ts);
                                })
                                .or()
                                .nested(cwSE -> {
                                    cwSE
                                            .isNotNull(Notice::getValidTimeEnd)
                                            .lt(Notice::getValidTimeEnd, ts);
                                });
                    }
                })
                .orderByDesc(BaseEntity::getCreateTime)
        );

        return new PageListVo<NoticeVo>()
            .setRecord(result.getRecords()
                .parallelStream()
                .map(NoticeDto::new)
                .map(NoticeDto::getVo)
                .map(notice -> {
                    String content = notice.getContent();
                    if(content == null) {
                        return notice;
                    }
                    final String transformerName = noticeSearchDto.getTransformer();
                    final NoticeTransformerEnum transformerEnum = NoticeTransformerEnum.find(transformerName);
                    if(transformerEnum == null) {
                        return notice;
                    }
                    final Function<String, String> contentTransformer = transformerEnum.getContentTransformer();
                    if(contentTransformer == null) {
                        return notice;
                    }
                    content = contentTransformer.apply(content);
                    notice.setContent(content);
                    return notice;
                })
                .collect(Collectors.toList()))
            .setSize(result.getSize())
            .setTotal(result.getTotal());
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "listNotice", allEntries = true)
        }
    )
    public Long createNotice(NoticeDto noticeDto) {
        if(CollUtil.isEmpty(noticeDto.getChannel())) {
            throw new GenshinApiException("公告频道不能为空");
        }

        Notice notice = noticeDto.getEntity();
        noticeMapper.insert(notice);
        return notice.getId();
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "listNotice", allEntries = true)
        }
    )
    public Boolean updateNotice(NoticeDto noticeDto) {
        if(CollUtil.isEmpty(noticeDto.getChannel())) {
            throw new GenshinApiException("公告频道不能为空");
        }

        return 1 == noticeMapper.update(
            noticeDto.getEntity(),
            Wrappers.<Notice>lambdaUpdate()
                .set(Notice::getValidTimeStart, noticeDto.getValidTimeStart())
                .set(Notice::getValidTimeEnd, noticeDto.getValidTimeEnd())
                .eq(Notice::getId, noticeDto.getId()));
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "listNotice", allEntries = true)
        }
    )
    public Boolean deleteNotice(Long noticeId) {
        int result = noticeMapper.deleteById(noticeId);
        return result == 1;
    }
}
