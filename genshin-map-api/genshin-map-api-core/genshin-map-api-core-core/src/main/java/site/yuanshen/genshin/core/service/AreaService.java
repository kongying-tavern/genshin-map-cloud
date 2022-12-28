package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.AreaDto;
import site.yuanshen.data.vo.AreaSearchVo;

import java.util.List;

/**
 * 地区服务接口
 *
 * @author Moment
 */
public interface AreaService {

    /**
     * 列出地区
     *
     * @param areaSearchVo 地区查询VO
     * @return 地区数据封装列表
     */
    List<AreaDto> listArea(AreaSearchVo areaSearchVo);

    /**
     * 获取单个地区信息
     *
     * @param areaId 地区ID
     * @return 地区数据封装
     */
    AreaDto getArea(Long areaId,List<Integer> hiddenFlagList);

    /**
     * 新增地区
     *
     * @param areaDto 地区数据封装
     * @return 新增地区ID
     */
    Long createArea(AreaDto areaDto);

    /**
     * 修改地区
     *
     * @param areaDto 地区数据封装
     * @return 是否成功
     */
    Boolean updateArea(AreaDto areaDto);

    /**
     * 递归删除地区
     *
     * @param areaId 地区ID
     * @return 是否成功
     */
    Boolean deleteArea(Long areaId);

}
