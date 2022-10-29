package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.PunctuateSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 打点审核服务接口
 *
 * @author Alex Fang
 */
public interface PunctuateAuditService {

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

}
