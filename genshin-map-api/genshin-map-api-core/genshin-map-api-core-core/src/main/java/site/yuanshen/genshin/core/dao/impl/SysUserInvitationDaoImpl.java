package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.SysUserInvitation;
import site.yuanshen.data.mapper.SysUserInvitationMapper;
import site.yuanshen.genshin.core.dao.SysUserInvitationDao;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserInvitationDaoImpl implements SysUserInvitationDao {
    private final SysUserInvitationMapper invitationMapper;

    @Override
    public String createInviteCode(String code) {
        if(StrUtil.isNotBlank(code)) {
            return code;
        }
        final String uuid = IdUtil.randomUUID();
        final String sign1 = StrUtil.sub(SecureUtil.sha256("site." + uuid), 0, 6);
        final String sign2 = StrUtil.sub(SecureUtil.md5(uuid + ".yuanshen"), 0, 8);
        final String newCode = String.format("%s-%s-%s", sign1, sign2, uuid);
        return newCode;
    }

    @Override
    public boolean validateInviteCode(String code) {
        if(StrUtil.isBlank(code)) {
            return false;
        }
        final List<String> chunks = StrUtil.split(code, '-', 3);
        if(chunks.size() < 3) {
            return false;
        }
        final String uuid = chunks.get(2);
        final String sign1 = StrUtil.sub(SecureUtil.sha256("site." + uuid), 0, 6);
        final String sign2 = StrUtil.sub(SecureUtil.md5(uuid + ".yuanshen"), 0, 8);
        final String newCode = String.format("%s-%s-%s", sign1, sign2, uuid);
        final boolean validate = newCode.equals(code);
        return validate;
    }

    @Override
    public Optional<SysUserInvitation> getInvitation(String code, String username) {
        if(StrUtil.isBlank(code) || StrUtil.isBlank(username)) {
            return Optional.empty();
        }
        List<SysUserInvitation> list = invitationMapper.selectList(Wrappers.<SysUserInvitation>lambdaQuery()
            .eq(SysUserInvitation::getCode, code)
            .eq(SysUserInvitation::getUsername, username)
        );
        if(list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public Optional<SysUserInvitation> getInvitation(String username) {
        if(StrUtil.isBlank(username)) {
            return Optional.empty();
        }
        List<SysUserInvitation> list = invitationMapper.selectList(Wrappers.<SysUserInvitation>lambdaQuery()
            .eq(SysUserInvitation::getUsername, username)
        );
        if(list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
