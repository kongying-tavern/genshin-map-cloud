package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.common.web.utils.UserUtils;
import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.dto.SysUserDeviceSearchDto;
import site.yuanshen.data.dto.adapter.BoolLogicPair;
import site.yuanshen.data.entity.SysUserDevice;
import site.yuanshen.data.enums.AccessPolicyEnum;
import site.yuanshen.data.mapper.SysUserDeviceMapper;
import site.yuanshen.data.vo.SysUserDeviceVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.SysUserDeviceDao;
import site.yuanshen.genshin.core.utils.ClientUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 用户设备详情服务
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserDeviceService {
    private final SysUserDeviceDao sysUserDeviceDao;
    private final SysUserDeviceMapper sysUserDeviceMapper;

    private static final String DEVICE_IP_DEFAULT = "N/A";
    private static final int DEVICE_ID_LEN_LIMIT = 500;

    /**
     * 检查设备是否有登录权限
     */
    public boolean checkDeviceAccess(Long userId, List<AccessPolicyEnum> accessPolicyList) {
        List<SysUserDeviceDto> deviceList = sysUserDeviceDao.getDeviceList(userId.toString());
        String ip = ClientUtils.getClientIpv4(DEVICE_IP_DEFAULT);
        String ua = StrUtil.sub(ClientUtils.getClientUa(), 0, DEVICE_ID_LEN_LIMIT);
        SysUserDeviceDto userDevice = sysUserDeviceDao.createNewDevice(userId, ip, ua);

        SysUserDeviceDto currentDevice = sysUserDeviceDao.findDevice(deviceList, userDevice);
        if(currentDevice == null) {
            currentDevice = sysUserDeviceDao.addNewDevice(userDevice);
        }
        boolean accessRes = checkDeviceAccessPolicies(deviceList, accessPolicyList, currentDevice);
        if(Objects.equals(accessRes, true)) {
            sysUserDeviceDao.updateDeviceLoginTime(currentDevice.getId());
        }
        return accessRes;
    }

    public boolean checkDeviceAccessPolicies(
            List<SysUserDeviceDto> deviceList,
            List<AccessPolicyEnum> accessPolicyList,
            SysUserDeviceDto currentDevice
    ) {
        if(currentDevice == null) {
            return false;
        }
        if(CollUtil.isEmpty(accessPolicyList)) {
            return true;
        }

        Boolean policyPass = null;
        for(AccessPolicyEnum accessPolicyEnum : accessPolicyList) {
            BiFunction<List<SysUserDeviceDto>, SysUserDeviceDto, BoolLogicPair> policyTester = accessPolicyEnum.getTester();
            if(policyTester != null) {
                BoolLogicPair policyResult = policyTester.apply(deviceList, currentDevice);
                if(policyResult != null) {
                    switch(policyResult.getLogic()) {
                        case OR:
                            policyPass = policyPass == null ? policyResult.getBoolValue() : policyPass | policyResult.getBoolValue();
                            break;
                        case AND:
                            policyPass = policyPass == null ? policyResult.getBoolValue() : policyPass & policyResult.getBoolValue();
                            break;
                    }
                    if(policyResult.getTruncated()) {
                        return policyPass;
                    }
                }
            }
        }

        return policyPass;
    }

    public PageListVo<SysUserDeviceVo> listPage(SysUserDeviceSearchDto deviceSearchDto) {
        QueryWrapper<SysUserDevice> wrapper = Wrappers.<SysUserDevice>query();

        // 处理排序
        final List<PgsqlUtils.Sort<SysUserDevice>> sortList = PgsqlUtils.toSortConfigurations(
            deviceSearchDto.getSort(),
            PgsqlUtils.SortConfig.<SysUserDevice>create()
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("id"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("deviceId"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("ipv4"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("status"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("lastLoginTime"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserDevice>create().withProp("updateTime"))
        );
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        LambdaQueryWrapper<SysUserDevice> queryWrapper = wrapper.lambda()
                .eq(SysUserDevice::getUserId, deviceSearchDto.getUserId())
                .like(StrUtil.isNotBlank(deviceSearchDto.getDeviceId()), SysUserDevice::getDeviceId, deviceSearchDto.getDeviceId())
                .like(StrUtil.isNotBlank(deviceSearchDto.getIpv4()), SysUserDevice::getIpv4, deviceSearchDto.getIpv4())
                .eq(deviceSearchDto.getStatus() != null, SysUserDevice::getStatus, deviceSearchDto.getStatus());

        Page<SysUserDevice> historyPage = sysUserDeviceMapper.selectPage(deviceSearchDto.getPageEntity(), queryWrapper);

        List<SysUserDeviceVo> result = historyPage.getRecords().stream()
                .map(SysUserDeviceDto::new)
                .map(SysUserDeviceDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<SysUserDeviceVo>()
                .setRecord(result)
                .setTotal(historyPage.getTotal())
                .setSize(historyPage.getSize());

    }

    public Boolean updateDevice(SysUserDeviceDto deviceDto) {
        if(deviceDto.getId() == null) {
            return false;
        }
        return 1 == sysUserDeviceMapper.update(null, Wrappers.<SysUserDevice>lambdaUpdate()
                .eq(SysUserDevice::getId, deviceDto.getId())
                .set(deviceDto.getStatus() != null, SysUserDevice::getStatus, deviceDto.getStatus())
                .set(SysUserDevice::getUpdateTime, TimeUtils.getCurrentTimestamp())
                .set(SysUserDevice::getUpdaterId, UserUtils.getUserId())
        );
    }
}
