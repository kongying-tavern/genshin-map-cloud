package site.yuanshen.data.dao;

import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

public interface MarkerDao {

    /**
     * @param isTestUser 是否是测试服打点用户
     * @return 点位总数
     */
    Long getMarkerCount(Boolean isTestUser);

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param isTestUser    是否是测试服打点用户
     * @return 点位完整信息的前端封装的分页记录
     */
    PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, Boolean isTestUser);

    /**
     * 通过bz2返回点位分页
     *
     * @param isTestUser 是否是测试打点用户
     * @param index      下标（从1开始）
     * @return 压缩后的字节数组
     */
    byte[] listPageMarkerByBz2(Boolean isTestUser, Long index);

    /**
     * 通过返回点位分页bz2的md5数组
     * @param isTestUser 是否是测试打点用户
     * @return 分页字节数组的md5
     */
    List<String> listMarkerBz2MD5(Boolean isTestUser);
}
