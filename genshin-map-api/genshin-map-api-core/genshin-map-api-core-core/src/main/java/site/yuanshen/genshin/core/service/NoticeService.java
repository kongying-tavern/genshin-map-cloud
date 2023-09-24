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
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.sql.Timestamp;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeMapper noticeMapper;

    @Cacheable(value = "listNotice")
    public PageListVo<NoticeVo> listNotice(NoticeSearchDto noticeSearchDto) {
        final Boolean isValid = noticeSearchDto.getGetValid();
        final Page<Notice> result = noticeMapper.selectPage(
            noticeSearchDto.getPageEntity(),
            Wrappers.<Notice>lambdaQuery()
                .in(CollUtil.isNotEmpty(noticeSearchDto.getChannels()), Notice::getChannel, noticeSearchDto.getChannels())
                .like(StrUtil.isNotBlank(noticeSearchDto.getTitle()), Notice::getTitle, noticeSearchDto.getTitle())
                .nested(isValid != null, cw -> {
                    final Timestamp ts = TimeUtils.getCurrentTimestamp();
                    if(isValid) {
                        cw
                            .le(Notice::getValidTimeStart, ts)
                            .ge(Notice::getValidTimeEnd, ts);
                    } else {
                        cw
                            .gt(Notice::getValidTimeStart, ts).or()
                            .lt(Notice::getValidTimeEnd, ts);
                    }
                })
                .orderByDesc(BaseEntity::getCreateTime)
        );

        return new PageListVo<NoticeVo>()
            .setRecord(result.getRecords()
                .parallelStream()
                .map(NoticeDto::new)
                .map(NoticeDto::getVo)
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
        if(StrUtil.isBlank(noticeDto.getChannel())) {
            throw new RuntimeException("公告频道不能为空");
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
        if(StrUtil.isBlank(noticeDto.getChannel())) {
            throw new RuntimeException("公告频道不能为空");
        }

        return 1 == noticeMapper.update(
            null,
            Wrappers.<Notice>lambdaUpdate()
                .setEntity(noticeDto.getEntity())
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
