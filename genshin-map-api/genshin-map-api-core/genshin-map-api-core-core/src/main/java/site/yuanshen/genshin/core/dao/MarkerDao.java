package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 点位信息的数据查询层
 *
 * @author Moment
 */
public interface MarkerDao {

    /**
     * @param hiddenFlagList hidden_flag范围
     * @return 点位总数
     */
    Long getMarkerCount(List<Integer> hiddenFlagList);

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param hiddenFlagList    hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList);

    /**
     * 按点位ID区间查询所有点位信息
     *
     * @param closeLeft  左闭下标
     * @param openRight  右开下标
     * @return 点位完整信息的前端封装的分页记录
     */
    List<MarkerVo> listMarkerIdRange(Long closeLeft, Long openRight);

    /**
     * 通过bz2返回点位分页
     *
     * @param index 下标（从1开始）
     * @return 压缩后的字节数组
     */
    byte[] listPageMarkerByBz2(Integer index);

    /**
     * 返回点位分页bz2的md5数组
     *
     * @return 分页字节数组的md5
     */
    List<String> listMarkerBz2MD5();
}
