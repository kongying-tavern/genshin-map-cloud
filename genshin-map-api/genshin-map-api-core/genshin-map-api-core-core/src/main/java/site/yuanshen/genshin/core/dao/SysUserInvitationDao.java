package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.entity.SysUserInvitation;

import java.util.Optional;

public interface SysUserInvitationDao {
    String createInviteCode(String code);

    boolean validateInviteCode(String code);

    Optional<SysUserInvitation> getInvitation(String code, String username);

    Optional<SysUserInvitation> getInvitation(String username);
}
