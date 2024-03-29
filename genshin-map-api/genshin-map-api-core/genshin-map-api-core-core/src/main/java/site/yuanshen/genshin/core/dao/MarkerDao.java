package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;
import java.util.Map;

/**
 * 点位信息的数据查询层
 *
 * @author Moment
 */
public interface MarkerDao {

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param hiddenFlagList    hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList);

    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList   点位ID列表
     * @param hiddenFlagList hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    List<MarkerVo> listMarkerById(List<Long> markerIdList, List<Integer> hiddenFlagList);

    /**
     * 通过bz2返回点位分页
     *
     * @param flagList 权限标记
     * @param md5 bz2数据的MD5
     * @return 压缩后的字节数组
     */
    byte[] listPageMarkerByBz2(List<Integer> flagList, String md5);

    /**
     * 返回MD5列表
     *
     * @param flagList 权限标记
     * @return 压缩后的字节数组
     */
    List<String> listMarkerMD5(List<Integer> flagList);

    /**
     * 刷新bz2返回点位分页
     * @return 刷新后的各个分页
     */
    Map<String, byte[]> refreshPageMarkerByBz2();

}
