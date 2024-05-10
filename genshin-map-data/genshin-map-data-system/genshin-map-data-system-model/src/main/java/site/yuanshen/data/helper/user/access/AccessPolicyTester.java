package site.yuanshen.data.helper.user.access;

import site.yuanshen.data.dto.SysUserDeviceDto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AccessPolicyTester {
    public static boolean testIpWithSameLastIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        if(currentDevice == null)
            return false;
        SysUserDeviceDto lastLoginDevice = deviceList.stream()
                .filter(v -> v != null && v.getLastLoginTime() != null)
                .sorted(Comparator.comparing(SysUserDeviceDto::getLastLoginTime).reversed())
                .findFirst()
                .orElse(null);
        if(lastLoginDevice == null)
            return true;
        return Objects.equals(lastLoginDevice.getIpv4(), currentDevice.getIpv4());
    }

    public static boolean testIpWithPassAllowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

    public static boolean testIpWithBlockDisallowIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
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
        return true;
    }

    public static boolean testDevWithPassAllowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

    public static boolean testDevWithBlockDisallowDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
    }

}
