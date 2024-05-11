package site.yuanshen.data.helper.user.access;

import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.enums.DeviceStatusEnum;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessPolicyTester {
    public static boolean testIpWithSameLastIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return true;
        return Objects.equals(lastLoginDevice.getIpv4(), currentDevice.getIpv4());
    }

    public static boolean testIpWithPassAllowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final Set<String> allowIps = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.VALID))
                .map(SysUserDeviceDto::getIpv4)
                .collect(Collectors.toSet());
        return allowIps.contains(currentDevice.getIpv4());
    }

    public static boolean testIpWithBlockDisallowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final Set<String> disallowIps = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.BLOCKED))
                .map(SysUserDeviceDto::getIpv4)
                .collect(Collectors.toSet());
        return !disallowIps.contains(currentDevice.getIpv4());
    }

    public static boolean testIpWithSameLastRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

    public static boolean testIpWithPassAllowRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

    public static boolean testIpWithBlockDisallowRegion(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

    public static boolean testDevWithSameLastDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return true;
        return Objects.equals(lastLoginDevice.getDeviceId(), currentDevice.getDeviceId());
    }

    public static boolean testDevWithPassAllowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final Set<String> allowDevices = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.VALID))
                .map(SysUserDeviceDto::getDeviceId)
                .collect(Collectors.toSet());
        return allowDevices.contains(currentDevice.getDeviceId());
    }

    public static boolean testDevWithBlockDisallowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        final Set<String> disallowDevices = deviceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus().equals(DeviceStatusEnum.BLOCKED))
                .map(SysUserDeviceDto::getDeviceId)
                .collect(Collectors.toSet());
        return !disallowDevices.contains(currentDevice.getDeviceId());
    }

}
