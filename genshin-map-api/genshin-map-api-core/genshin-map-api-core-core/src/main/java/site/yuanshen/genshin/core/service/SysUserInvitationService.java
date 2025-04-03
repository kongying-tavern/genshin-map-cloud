package site.yuanshen.genshin.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserInvitationConsumeDto;
import site.yuanshen.data.dto.SysUserInvitationDto;
import site.yuanshen.data.dto.SysUserInvitationSearchDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserInvitation;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserInvitationMapper;
import site.yuanshen.data.vo.SysUserInvitationConsumeResultVo;
import site.yuanshen.data.vo.SysUserInvitationSmallVo;
import site.yuanshen.data.vo.SysUserInvitationVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.SysUserDao;
import site.yuanshen.genshin.core.dao.SysUserInvitationDao;
import site.yuanshen.genshin.core.service.mbp.SysUserInvitationMBPService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserInvitationService {
    private final SysUserDao userDao;
    private final SysUserInvitationDao invitationDao;
    private final SysUserInvitationMapper invitationMapper;
    private final SysUserInvitationMBPService invitationMBPService;

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

    @Transactional
    public SysUserInvitationSmallVo updateInvitation(SysUserInvitationDto invitationDto) {
        String code =  invitationDto.getCode();
        String username = invitationDto.getUsername();

        if(StrUtil.isBlank(username)) {
            throw new GenshinApiException("用户名不能为空");
        }

        // 判断用户是否存在
        if(userDao.getUser(username).isPresent()) {
            throw new GenshinApiException("用户【" + username + "】已存在，无法邀请");
        }

        // 判断用户是否已存在邀请码不相同，但是相同名字的邀请条目
        Optional<SysUserInvitation> maybeInvitation = invitationDao.getInvitation(code, username);
        if(maybeInvitation.isEmpty()) {
            final Optional<SysUserInvitation> sameInvitation = invitationDao.getInvitation(username);
            if(sameInvitation.isPresent()) {
                throw new GenshinApiException("已存在用户【" + username + "】的邀请，请编辑已有邀请信息");
            }
        }

        // 初始化数据
        SysUserInvitation invitation = maybeInvitation.orElse(new SysUserInvitation());

        // 更新数据
        invitation.setCode(invitationDao.createInviteCode(invitation.getCode()));
        invitation.setUsername(username);
        invitation.setRoleId(ObjUtil.defaultIfNull(invitationDto.getRoleId(), RoleEnum.VISITOR.ordinal()));
        invitation.setRemark(StrUtil.blankToDefault(invitationDto.getRemark(), ""));
        invitation.setAccessPolicy(ObjUtil.defaultIfNull(invitationDto.getAccessPolicy(), List.of()));
        boolean isUpdate = invitation.getId() != null && invitation.getId() > 0;

        boolean success = invitationMBPService.saveOrUpdate(invitation);
        if(!success) {
            if(isUpdate)
                throw new GenshinApiException("编辑用户邀请失败");
            else
                throw new GenshinApiException("新增用户邀请失败");
        }

        final SysUserInvitationSmallVo invitationVo = BeanUtil.copyProperties(invitation, SysUserInvitationSmallVo.class);

        return invitationVo;
    }

    public SysUserInvitationSmallVo checkInvitation(SysUserInvitationSmallVo invitationVo) {
        String code = invitationVo.getCode();
        String username = invitationVo.getUsername();
        if(StrUtil.isBlank(code)) {
            throw new GenshinApiException("邀请码不能为空");
        } else if(StrUtil.isBlank(username)) {
            throw new GenshinApiException("用户名不能为空");
        }

        if(!invitationDao.validateInviteCode(code)) {
            throw new GenshinApiException("错误的邀请码，请重试");
        }

        SysUserInvitation foundInvitation = invitationDao.getInvitation(code, username).orElse(null);
        SysUserInvitationSmallVo result = null;
        if(foundInvitation != null) {
            result = BeanUtil.copyProperties(foundInvitation, SysUserInvitationSmallVo.class);
        }
        return result;
    }

    @Transactional
    public SysUserInvitationConsumeResultVo consumeInvitation(SysUserInvitationConsumeDto invitationDto) {
        String code = invitationDto.getCode();
        String username = invitationDto.getUsername();
        if(StrUtil.isBlank(username)) {
            throw new GenshinApiException("用户名不能为空");
        } else if(StrUtil.isBlank(code)) {
            throw new GenshinApiException("邀请码不能为空");
        } else if(!invitationDao.validateInviteCode(code)) {
            throw new GenshinApiException("错误的邀请码，请重试");
        }

        // 校验邀请数据
        SysUserInvitation foundInvitation = invitationDao.getInvitation(code, username).orElse(null);
        if(foundInvitation == null) {
            throw new GenshinApiException("错误的邀请码，请重试");
        }

        SysUserInvitationConsumeResultVo result = new SysUserInvitationConsumeResultVo();
        // 校验当前用户数据
        SysUser foundUser = userDao.getUser(username).orElse(null);
        if(foundUser != null) {
            result.setUserId(foundUser.getId());
            result.setResult(SysUserInvitationConsumeResultVo.Status.EXISTING);
            return result;
        }

        // 创建用户数据
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        SysUserDto sysUserDto = new SysUserDto();
        sysUserDto.setUsername(username);
        sysUserDto.setNickname(StrUtil.blankToDefault(invitationDto.getNickname(), ""));
        sysUserDto.setPassword(passwordEncoder.encode(invitationDto.getPassword()));
        sysUserDto.setRoleId(ObjUtil.defaultIfNull(foundInvitation.getRoleId(), RoleEnum.VISITOR.ordinal()));
        sysUserDto.setRemark(StrUtil.blankToDefault(foundInvitation.getRemark(), ""));
        sysUserDto.setAccessPolicy(ObjUtil.defaultIfNull(foundInvitation.getAccessPolicy(), List.of()));

        Long userId = userDao.insertUser(sysUserDto);
        result.setUserId(userId);
        result.setResult(SysUserInvitationConsumeResultVo.Status.SUCCESS);

        deleteInvitation(foundInvitation.getId());

        return result;
    }

    @Transactional
    public Boolean deleteInvitation(Long invitationId) {
        if(invitationId == null || invitationId <= 0) {
            return true;
        }

        invitationMapper.deleteById(invitationId);
        return true;
    }
}
