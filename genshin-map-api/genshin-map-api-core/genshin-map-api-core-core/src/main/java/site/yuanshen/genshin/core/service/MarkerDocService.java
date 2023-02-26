package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerExtraDto;
import site.yuanshen.data.dto.MarkerSingleDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 点位档案接口
 *
 * @author Moment
 */
public interface MarkerDocService {
    /**
     * 返回点位分页bz2的md5数组
     * @return 分页字节数组的md5
     */
    List<String> listMarkerBz2MD5();

    /**
     * @return 刷新点位分页bz2和对应的md5数组
     */
    List<String> refreshMarkerBz2MD5();

}
