package site.yuanshen.data.helper.user.access;

import site.yuanshen.data.dto.SysUserDeviceDto;

import java.util.List;

public class AccessPolicyTester {
    public static boolean testIpWithSameLastIp(List<SysUserDeviceDto> deviceList, SysUserDeviceDto currentDevice) {
        return true;
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
