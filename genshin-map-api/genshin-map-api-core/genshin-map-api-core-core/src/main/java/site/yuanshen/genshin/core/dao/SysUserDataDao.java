package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.proto.SysUserSmallVoOuterClass;
import site.yuanshen.data.vo.SysUserSmallVo;

public interface SysUserDataDao {
    SysUserSmallVoOuterClass.SysUserSmallVo buildSysUserSmallProto(SysUserSmallVo sysUserSmallVo);
}
