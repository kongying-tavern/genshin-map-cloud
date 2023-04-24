package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 点位服务接口
 *
 * @author Moment
 */
public interface MarkerService {

    //////////////START:点位自身的接口//////////////

    /**
     * 根据各种条件筛选查询点位ID
     *
     * @param markerSearchVo 点位查询前端封装
     * @return 点位ID列表
     */
    List<Long> searchMarkerId(MarkerSearchVo markerSearchVo);

    /**
     * 根据各种条件查询所有点位信息
     *
     * @param markerSearchVo 点位查询前端封装
     * @return 点位完整信息的数据封装列表
     */
    List<MarkerVo> searchMarker(MarkerSearchVo markerSearchVo);

    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList 点位ID列表
     * @return 点位完整信息的数据封装列表
     */
    List<MarkerVo> listMarkerById(List<Long> markerIdList, List<Integer> hiddenFlagList);


    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 点位完整信息的前端封装的分页记录
     */
    PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto,List<Integer> hiddenFlagList);

    /**
     * 新增点位（不包括额外字段）
     *
     * @param markerDto 点位无Extra的数据封装
     * @return 新点位ID
     */
    Long createMarker(MarkerDto markerDto);

    /**
     * 修改点位（不包括额外字段）
     *
     * @param markerDto 点位无Extra的数据封装
     * @return 是否成功
     */
    Boolean updateMarker(MarkerDto markerDto);

    /**
     * 根据点位ID删除点位
     *
     * @param markerId 点位ID
     * @return 是否成功
     */
    Boolean deleteMarker(Long markerId);

    //////////////END:点位自身的接口//////////////

}
