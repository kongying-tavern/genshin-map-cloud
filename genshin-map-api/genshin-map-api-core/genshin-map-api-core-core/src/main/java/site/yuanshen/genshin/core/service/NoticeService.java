package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeMapper noticeMapper;

    public PageListVo<NoticeVo> listNotice(NoticeSearchDto noticeSearchDto) {
        return new PageListVo<>();
    }

    public Long createNotice(NoticeDto noticeDto) {
        if(StrUtil.isBlank(noticeDto.getChannel())) {
            throw new RuntimeException("公告频道不能为空");
        }

        Notice notice = noticeDto.getEntity();
        noticeMapper.insert(notice);
        return notice.getId();
    }

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

    public Boolean deleteNotice(Long noticeId) {
        int result = noticeMapper.deleteById(noticeId);
        return result == 1;
    }
}
