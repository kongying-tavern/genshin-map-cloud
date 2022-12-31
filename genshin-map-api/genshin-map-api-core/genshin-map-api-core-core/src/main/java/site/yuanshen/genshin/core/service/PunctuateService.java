package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.helper.PageListVo;

/**
 * 打点服务接口
 *
 * @author Alex Fang
 */
public interface PunctuateService {

    //////////////START:打点员的接口//////////////

    /**
     * 分页查询所有打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 打点完整信息的数据封装列表
     */
    PageListVo<MarkerPunctuateVo> listPunctuatePage(PageSearchDto pageSearchDto);    //////////////START:打点员的接口//////////////

    /**
     * 分页查询自己提交的未通过的打点信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param authorId      打点员ID
     * @return 打点无额外字段的数据封装列表
     */
    PageListVo<MarkerPunctuateVo> listSelfPunctuatePage(PageSearchDto pageSearchDto, Long authorId);

    /**
     * 提交暂存点位
     *
     * @param markerSinglePunctuateDto 打点无额外字段的数据封装
     * @return 打点ID
     */
    Long addPunctuate(MarkerPunctuateDto markerSinglePunctuateDto);

    /**
     * 将暂存点位提交审核
     *
     * @param authorId 打点员ID
     * @return 是否成功
     */
    Boolean pushPunctuate(Long authorId);

    /**
     * 修改自身未提交的暂存点位
     *
     * @param singlePunctuateDto 打点无额外字段的数据封装
     * @return 是否成功
     */
    Boolean updateSelfPunctuate(MarkerPunctuateDto singlePunctuateDto);

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
