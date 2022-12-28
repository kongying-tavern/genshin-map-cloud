package site.yuanshen.data.enums;

import lombok.Getter;
import site.yuanshen.data.entity.SysRole;

@Getter
public enum RoleEnum {

    ADMIN("ADMIN", "系统管理员", 3,1+4),
    MAP_MANAGER("MAP_MANAGER", "地图管理者", 4,1+4),
    MAP_NEIGUI("MAP_NEIGUI", "打点测试员", 7,1+4),
    MAP_PUNCTUATE("MAP_PUNCTUATE", "地图打点员", 5,1),
    MAP_USER("MAP_USER", "地图用户", 6,1),
    VISITOR("VISITOR", "匿名用户", 100,1),
    ;

    private final String code;
    private final String name;
    private final int sort;

    private final int userDataLevel;

    RoleEnum(String code, String name, int sort,int userDataLevel) {
        this.code = code;
        this.name = name;
        this.sort = sort;
        this.userDataLevel = userDataLevel;
    }

    public SysRole getRoleBean() {
        SysRole roleBean = new SysRole();
        roleBean.setName(name);
        roleBean.setCode(code);
        roleBean.setSort(sort);
        return roleBean;
    }

    @Override
    public String toString() {
        return code;
    }
}
