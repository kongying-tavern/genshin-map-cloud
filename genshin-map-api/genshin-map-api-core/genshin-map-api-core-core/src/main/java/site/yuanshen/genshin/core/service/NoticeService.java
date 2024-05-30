package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.enums.notice.NoticeTransformerEnum;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.NoticeDao;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeDao noticeDao;
    private final NoticeMapper noticeMapper;

    public PageListVo<NoticeVo> listNotice(NoticeSearchDto noticeSearchDto) {
        final NoticeSearchDto searchDto = noticeDao.prepareGetListDto(noticeSearchDto);
        List<PgsqlUtils.Sort<Notice>> sortList = PgsqlUtils.toSortConfigurations(
            noticeSearchDto.getSort(),
            PgsqlUtils.SortConfig.<Notice>create()
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("id").withComparator(Comparator.comparingLong(Notice::getId)))
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("title").withComparator((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getTitle(), b.getTitle())))
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("sortIndex").withComparator(Comparator.comparingInt(Notice::getSortIndex)))
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("validTimeStart")
                    .withComparator((a, b) -> {
                        final Timestamp aTime = a.getValidTimeStart();
                        final long aTs = aTime == null ? Long.MIN_VALUE : aTime.getTime();
                        final Timestamp bTime = b.getValidTimeStart();
                        final long bTs = bTime == null ? Long.MIN_VALUE : bTime.getTime();
                        return Long.compare(aTs, bTs);
                    })
                )
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("validTimeEnd")
                    .withComparator((a, b) -> {
                        final Timestamp aTime = a.getValidTimeEnd();
                        final long aTs = aTime == null ? Long.MAX_VALUE : aTime.getTime();
                        final Timestamp bTime = b.getValidTimeEnd();
                        final long bTs = bTime == null ? Long.MAX_VALUE : bTime.getTime();
                        return Long.compare(aTs, bTs);
                    })
                )
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("isValid")
                    .withComparator((a, b) -> {
                        final long ts = TimeUtils.getCurrentTimestamp().getTime();
                        final Timestamp aTimeStart = a.getValidTimeStart();
                        final long aTsStart = aTimeStart == null ? Long.MIN_VALUE : aTimeStart.getTime();
                        final Timestamp aTimeEnd = a.getValidTimeEnd();
                        final long aTsEnd = aTimeEnd == null ? Long.MAX_VALUE : aTimeEnd.getTime();
                        final boolean aIsValid = aTsStart <= ts && ts <= aTsEnd;
                        final Timestamp bTimeStart = b.getValidTimeStart();
                        final long bTsStart = bTimeStart == null ? Long.MIN_VALUE : bTimeStart.getTime();
                        final Timestamp bTimeEnd = b.getValidTimeEnd();
                        final long bTsEnd = bTimeEnd == null ? Long.MAX_VALUE : bTimeEnd.getTime();
                        final boolean bIsValid = bTsStart <= ts && ts <= bTsEnd;
                        return Boolean.compare(aIsValid, bIsValid);
                    })
                )
                .addEntry(PgsqlUtils.SortConfigItem.<Notice>create().withProp("updateTime")
                    .withComparator((a, b) -> {
                        final Timestamp aTime = a.getUpdateTime();
                        final long aTs = aTime == null ? Long.MIN_VALUE : aTime.getTime();
                        final Timestamp bTime = b.getUpdateTime();
                        final long bTs = bTime == null ? Long.MIN_VALUE : bTime.getTime();
                        return Long.compare(aTs, bTs);
                    })
                )
        );
        List<Notice> fullList = noticeDao.getList(searchDto);
        fullList = noticeDao.postGetList(fullList, noticeSearchDto);
        fullList = PgsqlUtils.sortWrapper(fullList, sortList);
        List<Notice> list = CollUtil.sub(fullList, Math.toIntExact(noticeSearchDto.getCurrent()), Math.toIntExact(noticeSearchDto.getSize()));

        final PageListVo<NoticeVo> res = new PageListVo<NoticeVo>()
            .setRecord(list.stream()
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
                .collect(Collectors.toList())
            )
            .setSize(list.size())
            .setTotal(fullList.size());

        return res;
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

        final Notice notice = noticeDto.getEntity();
        notice.setValidTimeStart(null);
        notice.setValidTimeEnd(null);

        return 1 == noticeMapper.update(notice, Wrappers.<Notice>lambdaUpdate()
                .eq(Notice::getId, noticeDto.getId())
                .set(Notice::getValidTimeStart, noticeDto.getValidTimeStart())
                .set(Notice::getValidTimeEnd, noticeDto.getValidTimeEnd())
        );
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
