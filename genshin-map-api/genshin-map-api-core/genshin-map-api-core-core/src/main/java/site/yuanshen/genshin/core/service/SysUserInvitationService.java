package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.SysUserInvitationDto;
import site.yuanshen.data.dto.SysUserInvitationSearchDto;
import site.yuanshen.data.entity.SysUserInvitation;
import site.yuanshen.data.mapper.SysUserInvitationMapper;
import site.yuanshen.data.vo.SysUserInvitationVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.SysUserDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserInvitationService {
    private final SysUserDao userDao;
    private final SysUserInvitationMapper invitationMapper;

    public PageListVo<SysUserInvitationVo> searchInvitationPage(SysUserInvitationSearchDto invitationSearchDto) {
        QueryWrapper<SysUserInvitation> wrapper = Wrappers.<SysUserInvitation>query();
        final List<PgsqlUtils.Sort<SysUserInvitation>> sortList = PgsqlUtils.toSortConfigurations(
            invitationSearchDto.getSort(),
            PgsqlUtils.SortConfig.<SysUserInvitation>create()
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserInvitation>create().withProp("id"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserInvitation>create().withProp("username"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserInvitation>create().withProp("createTime"))
                .addEntry(PgsqlUtils.SortConfigItem.<SysUserInvitation>create().withProp("updateTime"))
        );
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        LambdaQueryWrapper<SysUserInvitation> queryWrapper = wrapper.lambda()
                .like(StrUtil.isNotBlank(invitationSearchDto.getCode()), SysUserInvitation::getCode, invitationSearchDto.getCode())
                .like(StrUtil.isNotBlank(invitationSearchDto.getUsername()), SysUserInvitation::getUsername, invitationSearchDto.getUsername());

        Page<SysUserInvitation> invitationPage = invitationMapper.selectPage(invitationSearchDto.getPageEntity(), queryWrapper);

        List<SysUserInvitationVo> result = invitationPage.getRecords().stream()
                .map(SysUserInvitationDto::new)
                .map(SysUserInvitationDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<SysUserInvitationVo>()
                .setRecord(result)
                .setTotal(invitationPage.getTotal())
                .setSize(invitationPage.getSize());
    }
}
