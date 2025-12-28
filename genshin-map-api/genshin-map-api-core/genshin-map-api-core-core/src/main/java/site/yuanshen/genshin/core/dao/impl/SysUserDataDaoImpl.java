package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.util.StrUtil;
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
        builder.setUsername(StrUtil.nullToEmpty(sysUserSmallVo.getUsername()));
        builder.setNickname(StrUtil.nullToEmpty(sysUserSmallVo.getNickname()));
        builder.setQq(StrUtil.nullToEmpty(sysUserSmallVo.getQq()));
        if (sysUserSmallVo.getPhone() != null) {
            builder.setPhone(sysUserSmallVo.getPhone());
        }
        if (sysUserSmallVo.getLogo() != null) {
            builder.setLogo(sysUserSmallVo.getLogo());
        }
        if (sysUserSmallVo.getRemark() != null) {
            builder.setRemark(sysUserSmallVo.getRemark());
        }

        final SysUserSmallVoOuterClass.SysUserSmallVo sysUserSmallVoProto = builder.build();

        return sysUserSmallVoProto;
    }
}
