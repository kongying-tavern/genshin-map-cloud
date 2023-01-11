package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.yuanshen.common.web.response.Codes;
import site.yuanshen.data.dto.HistorySearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.vo.HistoryVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;

public interface HistoryService extends IService<History> {

    PageListVo<HistoryVo> listPage(HistorySearchDto historySearchDto);

}
