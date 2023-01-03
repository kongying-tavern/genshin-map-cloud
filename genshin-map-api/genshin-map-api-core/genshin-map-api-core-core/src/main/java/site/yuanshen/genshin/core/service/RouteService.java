package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.RouteDto;
import site.yuanshen.data.dto.RouteSearchDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.RouteVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 路线服务接口
 *
 * @author Moment
 */
public interface RouteService {

    /**
     * 分页列出所有路线信息
     *
     * @param pageSearchDto  分页封装
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    PageListVo<RouteVo> listRoutePage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList);

    /**
     * 根据条件筛选分页查询路线信息
     *
     * @param routeSearchDto 路线分页查询数据封装
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    PageListVo<RouteVo> listRoutePageSearch(RouteSearchDto routeSearchDto, List<Integer> hiddenFlagList);

    /**
     * 根据id列表查询路线信息
     *
     * @param idList         路线ID列表
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    List<RouteDto> listRouteById(List<Long> idList, List<Integer> hiddenFlagList);

    /**
     * 新增路线
     *
     * @param routeDto 路线数据封装
     * @return 新增路线ID
     */
    Long createRoute(RouteDto routeDto);

    /**
     * 修改路线
     *
     * @param routeDto 路线数据封装
     * @return 是否成功
     */
    Boolean updateRoute(RouteDto routeDto);

    /**
     * 删除路线
     *
     * @param routeId 路线ID
     * @return 是否成功
     */
    Boolean deleteRoute(Long routeId);

}
