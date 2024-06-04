package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.entity.Notice;

import java.util.List;

/**
 * 公告的数据查询层
 *
 * @author Alex Fang
 */
public interface NoticeDao {

    /**
     * 准备获取列表的参数，用以防止因为多余字段导致的缓存失效
     */
    NoticeSearchDto prepareGetListDto(NoticeSearchDto noticeSearchDto);

    /**
     * 获取列表
     */
    List<Notice> getList(NoticeSearchDto noticeSearchDto);

    /**
     * 获取列表后处理
     */
    List<Notice> postGetList(List<Notice> list, NoticeSearchDto noticeSearchDto);
}
