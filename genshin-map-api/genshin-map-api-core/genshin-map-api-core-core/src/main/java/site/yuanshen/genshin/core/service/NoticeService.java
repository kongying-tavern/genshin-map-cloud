package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.enums.notice.NoticeTransformerEnum;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.NoticeDao;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeDao noticeDao;
    private final NoticeMapper noticeMapper;

    public PageListVo<NoticeVo> listNotice(NoticeSearchDto noticeSearchDto) {
        final NoticeSearchDto searchDto = noticeDao.prepareGetListDto(noticeSearchDto);
        List<Notice> fullList = noticeDao.getList(searchDto);
        fullList = noticeDao.postGetList(fullList, noticeSearchDto);
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
