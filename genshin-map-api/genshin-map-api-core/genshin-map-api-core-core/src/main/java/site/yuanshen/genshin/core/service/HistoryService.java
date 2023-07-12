package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.HistoryDto;
import site.yuanshen.data.dto.HistorySearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.data.vo.HistoryVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService extends ServiceImpl<HistoryMapper, History> {

    private final HistoryMapper historyMapper;

    public PageListVo<HistoryVo> listPage(HistorySearchDto historySearchDto) {
        Page<History> historyPage = historyMapper.selectPage(historySearchDto.getPageEntity(),
                Wrappers.<History>lambdaQuery()
                        .eq(ObjectUtil.isNotNull(historySearchDto.getType()),History::getType, historySearchDto.getType())
                        .in(!historySearchDto.getId().isEmpty(),History::getTId, historySearchDto.getId()));

        List<HistoryVo> result = historyPage.getRecords().stream()
                .map(HistoryDto::new)
                .map(HistoryDto::getVo)
                .sorted(Comparator.comparing(HistoryVo::getUpdateTime)).collect(Collectors.toList());
        return new PageListVo<HistoryVo>()
                .setRecord(result)
                .setTotal(historyPage.getTotal())
                .setSize(historyPage.getSize());
    }
}


