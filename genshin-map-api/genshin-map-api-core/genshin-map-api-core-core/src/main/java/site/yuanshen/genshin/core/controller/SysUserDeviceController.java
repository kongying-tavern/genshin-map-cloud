package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.dto.SysUserDeviceSearchDto;
import site.yuanshen.data.vo.SysUserDeviceSearchVo;
import site.yuanshen.data.vo.SysUserDeviceVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysUserDeviceService;

/**
 * 系统用户设备API
 *
 * @author Alex Fang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/device")
@Tag(name = "device", description = "系统用户设备API")
public class SysUserDeviceController {

    private final SysUserDeviceService userDeviceService;

    /**
     * 用户设备信息批量查询
     *
     * @param searchVo 用户设备列表查询前端封装
     * @return 用户设备信息封装List
     */
    @Operation(summary = "查询用户设备列表", description = "查询用户设备列表")
    @PostMapping("/list")
    public R<PageListVo<SysUserDeviceVo>> searchPage(@RequestBody SysUserDeviceSearchVo searchVo) {
        SysUserDeviceSearchDto searchDto = new SysUserDeviceSearchDto(searchVo);
        if(searchDto.getUserId() == null) {
            throw new GenshinApiException("用户ID不能为空");
        }
        return RUtils.create(userDeviceService.listPage(searchDto));
    }

    @Operation(summary = "更新用户设备信息", description = "更新用户设备信息")
    @PostMapping("/update")
    public R<Boolean> updateDevice(@RequestBody SysUserDeviceVo updateVo) {
        final Boolean success = userDeviceService.updateDevice(new SysUserDeviceDto(updateVo));
        return RUtils.create(success);
    }
}
