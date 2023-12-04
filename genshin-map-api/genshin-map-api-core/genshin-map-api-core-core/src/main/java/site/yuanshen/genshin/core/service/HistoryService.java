package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.HistoryDto;
import site.yuanshen.data.dto.HistorySearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.data.vo.HistoryVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService extends ServiceImpl<HistoryMapper, History> {

    private final HistoryMapper historyMapper;

    public PageListVo<HistoryVo> listPage(HistorySearchDto historySearchDto) {
        QueryWrapper<History> wrapper = Wrappers.<History>query();
        // 处理排序
        final List<PgsqlUtils.Sort<History>> sortList = PgsqlUtils.toSort(historySearchDto.getSort(), History.class, Set.of("updateTime"));
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        LambdaQueryWrapper<History> queryWrapper = wrapper.lambda()
                .eq(ObjectUtil.isNotNull(historySearchDto.getType()), History::getType, historySearchDto.getType())
                .in(!historySearchDto.getId().isEmpty(), History::getTId, historySearchDto.getId());

        Page<History> historyPage = historyMapper.selectPage(historySearchDto.getPageEntity(), queryWrapper);

        List<HistoryVo> result = historyPage.getRecords().stream()
                .map(HistoryDto::new)
                .map(HistoryDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<HistoryVo>()
                .setRecord(result)
                .setTotal(historyPage.getTotal())
                .setSize(historyPage.getSize());
    }
}


