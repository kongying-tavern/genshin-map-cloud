package site.yuanshen.data.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.helper.user.access.AccessPolicyTester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public enum AccessPolicyEnum {
    IP$$SAME_LAST_IP(
            "ip:same_last_ip", "与最后一次登录IP相同",
            AccessPolicyTester::testIpWithSameLastIp
    ),
    IP$$PASS_ALLOW_IP(
            "ip:pass_allow_ip", "对列表中有效的IP放行",
            AccessPolicyTester::testIpWithPassAllowIp
    ),
    IP$$BLOCK_DISALLOW_IP(
            "ip:block_disallow_ip", "对列表中禁用的IP拦截",
            AccessPolicyTester::testIpWithBlockDisallowIp
    ),
    IP$$SAME_LAST_REGION(
            "ip:same_last_region", "与最后一次登录地区相同",
            AccessPolicyTester::testIpWithSameLastRegion
    ),
    IP$$PASS_ALLOW_REGION(
            "ip:pass_allow_region", "对列表中有效的地区放行",
            AccessPolicyTester::testIpWithPassAllowRegion
    ),
    IP$$BLOCK_DISALLOW_REGION(
            "ip:block_disallow_region", "对列表中禁用的地区拦截",
            AccessPolicyTester::testIpWithBlockDisallowRegion
    ),
    DEV$$SAME_LAST_DEVICE(
            "dev:same_last_device", "与最后一次登录设备相同",
            AccessPolicyTester::testDevWithSameLastDevice
    ),
    DEV$$PASS_ALLOW_DEVICE(
            "dev:pass_allow_device", "对列表中有效的设备放行",
            AccessPolicyTester::testDevWithPassAllowDevice
    ),
    DEV$$BLOCK_DISALLOW_DEVICE(
            "dev:block_disallow_device", "对列表中禁用的设备拦截",
            AccessPolicyTester::testDevWithBlockDisallowDevice
    );

    @Getter
    private final String code;

    @Getter
    private final String desc;

    @Getter
    private final BiFunction<List<SysUserDeviceDto>, SysUserDeviceDto, Boolean> tester;

    private static Map<String, AccessPolicyEnum> getAccessPolicyMap() {
        Map<String, AccessPolicyEnum> policyMap = new HashMap<>();
        for(AccessPolicyEnum policy : AccessPolicyEnum.values()) {
            policyMap.put(policy.getCode(), policy);
        }
        return policyMap;
    }

    public static List<AccessPolicyEnum> getPoliciesFromCodes(List<String> codes) {
        if(CollUtil.isEmpty(codes)) {
            return new ArrayList<>();
        }

        final Map<String, AccessPolicyEnum> policyMap = getAccessPolicyMap();
        final List<AccessPolicyEnum> policyList = new ArrayList<>();
        for(String code : codes) {
            final AccessPolicyEnum policyEnum = policyMap.get(code);
            if(policyEnum != null) {
                policyList.add(policyEnum);
            }
        }
        return policyList;
    }

}
