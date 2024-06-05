package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.SysActionLogDto;
import site.yuanshen.data.dto.SysActionLogSearchDto;
import site.yuanshen.data.entity.SysActionLog;
import site.yuanshen.data.mapper.SysActionLogMapper;
import site.yuanshen.data.vo.SysActionLogVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.utils.ClientUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户设备详情服务
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysActionLogService {

    private final SysActionLogMapper sysActionLogMapper;

    /**
     * 添加新操作日志
     */
    public void addNewLog(
        Long userId,
        String action,
        boolean isError,
        Map<String, Object> extraData
    ) {
        final ClientUtils.ClientInfo clientInfo = ClientUtils.getClientInfo(null, null);
        addNewLog(userId, action, isError, extraData, clientInfo);
    }

    /**
     * 添加新操作日志
     */
    public void addNewLog(
        Long userId,
        String action,
        boolean isError,
        Map<String, Object> extraData,
        ClientUtils.ClientInfo clientInfo
    ) {
        if(extraData == null) {
            extraData = new HashMap<>();
        }

        final SysActionLog actionLog = new SysActionLog()
            .withUserId(userId)
            .withAction(action)
            .withIpv4(clientInfo.getIpv4())
            .withDeviceId(clientInfo.getUa())
            .withIsError(isError)
            .withExtraData(extraData);

        sysActionLogMapper.insert(actionLog);
    }

    public PageListVo<SysActionLogVo> listPage(SysActionLogSearchDto sysActionLogSearchDto) {
        QueryWrapper<SysActionLog> wrapper = Wrappers.<SysActionLog>query();

        // 处理排序
        final List<PgsqlUtils.Sort<SysActionLog>> sortList = PgsqlUtils.toSortConfigurations(
            sysActionLogSearchDto.getSort(),
            PgsqlUtils.SortConfig.<SysActionLog>create()
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("id"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("deviceId"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("ipv4"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("action"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("isError"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysActionLog>create().withProp("updateTime"))
        );
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        LambdaQueryWrapper<SysActionLog> queryWrapper = wrapper.lambda()
                .eq(sysActionLogSearchDto.getUserId() != null, SysActionLog::getUserId, sysActionLogSearchDto.getUserId())
                .like(StrUtil.isNotBlank(sysActionLogSearchDto.getDeviceId()), SysActionLog::getDeviceId, sysActionLogSearchDto.getDeviceId())
                .like(StrUtil.isNotBlank(sysActionLogSearchDto.getIpv4()), SysActionLog::getIpv4, sysActionLogSearchDto.getIpv4())
                .eq(StrUtil.isNotBlank(sysActionLogSearchDto.getAction()), SysActionLog::getAction, sysActionLogSearchDto.getAction())
                .eq(sysActionLogSearchDto.getIsError() != null, SysActionLog::getIsError, sysActionLogSearchDto.getIsError());

        Page<SysActionLog> historyPage = sysActionLogMapper.selectPage(sysActionLogSearchDto.getPageEntity(), queryWrapper);

        List<SysActionLogVo> result = historyPage.getRecords().stream()
                .map(SysActionLogDto::new)
                .map(SysActionLogDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<SysActionLogVo>()
                .setRecord(result)
                .setTotal(historyPage.getTotal())
                .setSize(historyPage.getSize());
    }
}
