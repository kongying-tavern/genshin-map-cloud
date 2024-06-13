package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.RouteDto;
import site.yuanshen.data.dto.RouteSearchDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Route;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.RouteMapper;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.RouteVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 路线服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteMapper routeMapper;
    private final SysUserMapper userMapper;

    /**
     * 分页列出所有路线信息
     *
     * @param pageSearchDto  分页封装
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    public PageListVo<RouteVo> listRoutePage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList) {
        Page<Route> routePage = routeMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<Route>lambdaQuery().in(!hiddenFlagList.isEmpty(), Route::getHiddenFlag, hiddenFlagList)
                .orderByAsc(Route::getId));
        PageListVo<RouteVo> page = new PageListVo<RouteVo>()
                .setRecord(routePage.getRecords().parallelStream().map(RouteDto::new).map(RouteDto::getVo).collect(Collectors.toList()))
                .setSize(routePage.getSize())
                .setTotal(routePage.getTotal());
        return page;
    }

    /**
     * 根据条件筛选分页查询路线信息
     *
     * @param searchDto      路线分页查询数据封装
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    public PageListVo<RouteVo> listRoutePageSearch(RouteSearchDto searchDto, List<Integer> hiddenFlagList) {
        String namePart = searchDto.getNamePart();
        String nicknamePart = searchDto.getCreatorNicknamePart();
        String creatorId = searchDto.getCreatorId();
        Page<Route> routePage = routeMapper.selectPage(searchDto.getPage(),
                Wrappers.<Route>lambdaQuery()
                        .orderByAsc(Route::getId)
                        .like(StrUtil.isNotBlank(namePart), Route::getName, namePart)
                        .like(StrUtil.isNotBlank(nicknamePart), Route::getCreatorNickname, nicknamePart)
                        .eq(StrUtil.isNotBlank(creatorId), BaseEntity::getCreatorId, creatorId));
        PageListVo<RouteVo> page = new PageListVo<RouteVo>()
                .setRecord(routePage.getRecords().parallelStream()
                        .filter(route -> hiddenFlagList.contains(route.getHiddenFlag()))
                        .map(RouteDto::new)
                        .map(RouteDto::getVo)
                        .collect(Collectors.toList()))
                .setSize(routePage.getSize())
                .setTotal(routePage.getTotal());
        return page;
    }

    /**
     * 根据id列表查询路线信息
     *
     * @param idList         路线ID列表
     * @param hiddenFlagList 显隐等级List
     * @return 路线信息分页封装
     */
    public List<RouteDto> listRouteById(List<Long> idList, List<Integer> hiddenFlagList) {
        List<RouteDto> result = routeMapper.selectBatchIds(idList).parallelStream()
                .filter(route -> hiddenFlagList.contains(route.getHiddenFlag()))
                .map(RouteDto::new)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 新增路线
     *
     * @param routeDto 路线数据封装
     * @return 新增路线ID
     */
    public Long createRoute(RouteDto routeDto) {
        SysUser user = getUserNotNull(routeDto.getUpdaterId());
        routeDto.setCreatorNickname(StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername());
        Route entity = routeDto.getEntity();
        entity.setVersion(0L);
        routeMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 修改路线
     *
     * @param routeDto 路线数据封装
     * @return 是否成功
     */
    public Boolean updateRoute(RouteDto routeDto) {
        return 1 == routeMapper.updateById(
                routeDto.getEntity());
    }

    /**
     * 删除路线
     *
     * @param routeId 路线ID
     * @return 是否成功
     */
    public Boolean deleteRoute(Long routeId) {
        return routeMapper.deleteById(routeId) == 1;
    }

    private SysUser getUserNotNull(Long id) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getId, id)))
                .orElseThrow(() -> new GenshinApiException("用户不存在"));
    }
}
