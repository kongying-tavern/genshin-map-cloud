package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.*;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.*;
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
    List<MarkerDto> searchMarker(MarkerSearchVo markerSearchVo);

    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList 点位ID列表
     * @return 点位完整信息的数据封装列表
     */
    List<MarkerDto> listMarkerById(List<Long> markerIdList);

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 点位完整信息的前端封装的分页记录
     */
    PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto);

    /**
     * 新增点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 新点位ID
     */
    Long createMarker(MarkerSingleDto markerSingleDto);

    /**
     * 新增点位额外字段信息
     *
     * @param markerExtraDto 点位额外信息的数据封装
     * @return 是否成功
     */
    Boolean addMarkerExtra(MarkerExtraDto markerExtraDto);

    /**
     * 修改点位（不包括额外字段）
     *
     * @param markerSingleDto 点位无Extra的数据封装
     * @return 是否成功
     */
    Boolean updateMarker(MarkerSingleDto markerSingleDto);

    /**
     * 修改点位额外字段
     *
     * @param markerExtraDto 点位额外信息的数据封装
     * @return 是否成功
     */
    Boolean updateMarkerExtra(MarkerExtraDto markerExtraDto);

    /**
     * 根据点位ID删除点位
     *
     * @param markerId 点位ID
     * @return 是否成功
     */
    Boolean deleteMarker(Long markerId);

    //////////////END:点位自身的接口//////////////

    //////////////START:审核员的接口//////////////

    /**
     * 根据各种条件筛选打点ID
     *
     * @param punctuateSearchVo 打点查询前端封装
     * @return 打点ID列表
     */
    List<Long> searchPunctuateId(PunctuateSearchVo punctuateSearchVo);

    /**
     * 根据各种条件筛选打点信息
     *
     * @param punctuateSearchVo 打点查询前端封装
     * @return 打点ID列表
     */
    List<MarkerPunctuateDto> searchPunctuate(PunctuateSearchVo punctuateSearchVo);

    /**
     * 通过打点ID列表查询打点信息
     *
     * @param punctuateIdList 打点ID列表
     * @return 打点完整信息的数据封装列表
     */
    List<MarkerPunctuateDto> listPunctuateById(List<Long> punctuateIdList);

    /**
     * 分页查询所有打点信息（包括暂存）
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的前端分页记录封装
     */
    PageListVo<MarkerPunctuateVo> listAllPunctuatePage(PageSearchDto pageSearchDto);

    /**
     * 通过点位审核
     *
     * @param punctuateId 打点ID
     * @return 点位ID
     */
    Long passPunctuate(Long punctuateId);

    /**
     * 驳回点位审核
     *
     * @param punctuateId 打点ID
     * @return 是否成功
     */
    Boolean rejectPunctuate(Long punctuateId);

    /**
     * 删除提交点位
     *
     * @param punctuateId 打点ID
     * @return 是否成功
     */
    Boolean deletePunctuate(Long punctuateId);

    //////////////END:审核员的接口//////////////

    //////////////START:打点员的接口//////////////

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    PageListVo<MarkerPunctuateVo> listPunctuatePage(PageSearchDto pageSearchDto);    //////////////START:打点员的接口//////////////

    /**
     * 分页查询自己提交的未通过的打点信息（不包含额外字段）
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点无额外字段的数据封装列表
     */
    PageListVo<MarkerSinglePunctuateVo> listSelfSinglePunctuatePage(PageSearchDto pageSearchDto, Long authorId);

    /**
     * 分页查询自己提交的未通过的打点信息（只包含额外字段）
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点只有额外字段的数据封装列表
     */
    PageListVo<MarkerExtraPunctuateVo> listSelfExtraPunctuatePage(PageSearchDto pageSearchDto, Long authorId);

    /**
     * 提交暂存点位（不含额外字段）
     *
     * @param markerSinglePunctuateDto 打点无额外字段的数据封装
     * @return 打点ID
     */
    Long addSinglePunctuate(MarkerSinglePunctuateDto markerSinglePunctuateDto);

    /**
     * 提交暂存点位额外字段
     *
     * @param markerExtraPunctuateDto 打点额外字段
     * @return 是否成功
     */
    Boolean addExtraPunctuate(MarkerExtraPunctuateDto markerExtraPunctuateDto);

    /**
     * 将暂存点位提交审核
     *
     * @param authorId 打点员ID
     * @return 是否成功
     */
    Boolean pushPunctuate(Long authorId);

    /**
     * 修改自身未提交的暂存点位（不包括额外字段）
     *
     * @param singlePunctuateDto 打点无额外字段的数据封装
     * @return 是否成功
     */
    Boolean updateSelfSinglePunctuate(MarkerSinglePunctuateDto singlePunctuateDto);

    /**
     * 修改自身未提交的暂存点位的额外字段
     *
     * @param extraPunctuateDto 打点额外字段
     * @return 是否成功
     */
    Boolean updateSelfPunctuateExtra(MarkerExtraPunctuateDto extraPunctuateDto);

    /**
     * 删除自己未通过的提交点位
     *
     * @param punctuateId 打点ID
     * @param authorId    打点员ID
     * @return 是否成功
     */
    Boolean deleteSelfPunctuate(Long punctuateId, Long authorId);

    //////////////END:打点员的接口//////////////

}
