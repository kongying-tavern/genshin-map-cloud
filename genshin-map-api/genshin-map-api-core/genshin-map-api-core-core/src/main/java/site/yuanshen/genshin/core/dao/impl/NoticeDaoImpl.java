package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.mapper.NoticeMapper;
import site.yuanshen.genshin.core.dao.NoticeDao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 公告的数据查询层
 *
 * @author Alex Fang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeDaoImpl implements NoticeDao {
    private final NoticeMapper noticeMapper;

    @Override
    public NoticeSearchDto prepareGetListDto(NoticeSearchDto noticeSearchDto) {
        NoticeSearchDto searchDto = new NoticeSearchDto();
        BeanUtils.copyNotNull(noticeSearchDto, searchDto);
        // 移除分页条件
        searchDto.setCurrent(null);
        searchDto.setSize(null);
        // 移除排序字段
        searchDto.setSort(List.of());
        // 移除动态数据过滤条件
        searchDto.setGetValid(null);
        // 移除附加字段
        searchDto.setTransformer(null);

        return searchDto;
    }

    @Override
    @Cacheable("listNotice")
    public List<Notice> getList(NoticeSearchDto noticeSearchDto) {
        // 预处理参数
        // 字段：频道
        String searchChannelsArr = "";
        String searchChannelsSegment = "";
        if(CollUtil.isNotEmpty(noticeSearchDto.getChannels())) {
            final List<String> channels = noticeSearchDto.getChannels();
            final List<String> channelArr = channels.stream().map(channel -> "'" + channel + "'").collect(Collectors.toList());
            searchChannelsArr = StrUtil.join(",", channelArr);
            searchChannelsSegment = String.format("(\"channel\"::jsonb) ??| array[%s]", searchChannelsArr);
        }

        // 获取数据
        final LambdaQueryWrapper<Notice> wrapper = Wrappers.<Notice>lambdaQuery()
                .apply(StrUtil.isNotBlank(searchChannelsSegment), searchChannelsSegment)
                .like(StrUtil.isNotBlank(noticeSearchDto.getTitle()), Notice::getTitle, noticeSearchDto.getTitle());
        final List<Notice> result = noticeMapper.selectList(wrapper);

        return result;
    }

    @Override
    public List<Notice> postGetList(List<Notice> list, NoticeSearchDto noticeSearchDto) {
        Boolean getValid = noticeSearchDto.getGetValid();
        if(getValid == null) {
            return list;
        }

        Timestamp ts = TimeUtils.getCurrentTimestamp();

        return list.parallelStream()
            .filter(Objects::nonNull)
            .filter(v -> {
                Timestamp validTimeStart = v.getValidTimeStart();
                Timestamp validTimeEnd = v.getValidTimeEnd();
                boolean afterStart = validTimeStart == null || validTimeStart.before(ts);
                boolean beforeEnd = validTimeEnd == null || validTimeEnd.after(ts);
                boolean inRange = afterStart && beforeEnd;
                return getValid ? inRange : !inRange;
            })
            .collect(Collectors.toList());
    }
}
