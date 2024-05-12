package site.yuanshen.data.helper.user.access;

import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.dto.adapter.BoolLogicPair;
import site.yuanshen.data.enums.DeviceStatusEnum;
import site.yuanshen.data.enums.LogicEnum;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessPolicyTester {
    public static BoolLogicPair testIpWithSameLastIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return BoolLogicPair.create(true, logic, truncated);
        final boolean isAccess = Objects.equals(lastLoginDevice.getIpv4(), currentDevice.getIpv4());
        return BoolLogicPair.create(isAccess, logic, truncated);
    }

    public static BoolLogicPair testIpWithPassAllowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> allowIps = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.VALID))
                .map(SysUserDeviceDto::getIpv4)
                .collect(Collectors.toSet());
        final boolean isAccess = allowIps.contains(currentDevice.getIpv4());
        return BoolLogicPair.create(isAccess, logic, truncated);
    }

    public static BoolLogicPair testIpWithBlockDisallowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.AND;
        final boolean truncated = true;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> disallowIps = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.BLOCKED))
                .map(SysUserDeviceDto::getIpv4)
                .collect(Collectors.toSet());
        final boolean isAccess = !disallowIps.contains(currentDevice.getIpv4());
        return BoolLogicPair.create(isAccess, logic, isAccess != truncated);
    }

    public static BoolLogicPair testIpWithSameLastRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return BoolLogicPair.create(true, logic, truncated);
        final String lastLoginRegionHash = lastLoginDevice.getIpRegion() != null ? lastLoginDevice.getIpRegion().getHash() : "";
        final String currentRegionHash = currentDevice.getIpRegion() != null ? currentDevice.getIpRegion().getHash() : "";
        final boolean isAccess = Objects.equals(lastLoginRegionHash, currentRegionHash);
        return BoolLogicPair.create(isAccess, logic, truncated);
    }

    public static BoolLogicPair testIpWithPassAllowRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        else if(currentDevice.getIpRegion() == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> allowRegions = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.VALID))
                .filter(v -> v.getIpRegion() != null)
                .map(v -> v.getIpRegion().getHash())
                .collect(Collectors.toSet());
        final boolean isAllowRegion = allowRegions.contains(currentDevice.getIpRegion().getHash());
        return BoolLogicPair.create(isAllowRegion, logic, truncated);
    }

    public static BoolLogicPair testIpWithBlockDisallowRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.AND;
        final boolean truncated = true;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        else if(currentDevice.getIpRegion() == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> disallowRegions = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.BLOCKED))
                .filter(v -> v.getIpRegion() != null)
                .map(v -> v.getIpRegion().getHash())
                .collect(Collectors.toSet());
        final boolean isAccess = !disallowRegions.contains(currentDevice.getIpRegion().getHash());
        return BoolLogicPair.create(isAccess, logic, isAccess != truncated);
    }

    public static BoolLogicPair testDevWithSameLastDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return BoolLogicPair.create(true, logic, truncated);
        final boolean isAccess = Objects.equals(lastLoginDevice.getDeviceId(), currentDevice.getDeviceId());
        return BoolLogicPair.create(isAccess, logic, truncated);
    }

    public static BoolLogicPair testDevWithPassAllowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.OR;
        final boolean truncated = false;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> allowDevices = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.VALID))
                .map(SysUserDeviceDto::getDeviceId)
                .collect(Collectors.toSet());
        final boolean isAccess = allowDevices.contains(currentDevice.getDeviceId());
        return BoolLogicPair.create(isAccess, logic, truncated);
    }

    public static BoolLogicPair testDevWithBlockDisallowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        final LogicEnum logic = LogicEnum.AND;
        final boolean truncated = true;

        if(currentDevice == null)
            return BoolLogicPair.create(false, logic, truncated);
        final Set<String> disallowDevices = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.BLOCKED))
                .map(SysUserDeviceDto::getDeviceId)
                .collect(Collectors.toSet());
        final boolean isAccess = !disallowDevices.contains(currentDevice.getDeviceId());
        return BoolLogicPair.create(isAccess, logic, isAccess != truncated);
    }

}
