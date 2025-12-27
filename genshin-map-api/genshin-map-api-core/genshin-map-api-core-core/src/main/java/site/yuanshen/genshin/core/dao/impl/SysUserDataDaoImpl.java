package site.yuanshen.genshin.core.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.proto.SysUserSmallVoOuterClass;
import site.yuanshen.data.vo.SysUserSmallVo;
import site.yuanshen.genshin.core.dao.SysUserDataDao;

@Slf4j
@Service
public class SysUserDataDaoImpl implements SysUserDataDao {
    @Override
    public SysUserSmallVoOuterClass.SysUserSmallVo buildSysUserSmallProto(SysUserSmallVo sysUserSmallVo) {
        if (sysUserSmallVo == null) {
            return null;
        }

        final SysUserSmallVoOuterClass.SysUserSmallVo.Builder builder = SysUserSmallVoOuterClass.SysUserSmallVo.newBuilder();
        builder
            .setUsername(sysUserSmallVo.getUsername())
            .setNickname(sysUserSmallVo.getNickname())
            .setQq(sysUserSmallVo.getQq())
            .setPhone(sysUserSmallVo.getPhone())
            .setLogo(sysUserSmallVo.getLogo())
            .setRemark(sysUserSmallVo.getRemark());
        final SysUserSmallVoOuterClass.SysUserSmallVo sysUserSmallVoProto = builder.build();

        return sysUserSmallVoProto;
    }
}
