package site.yuanshen.data.enums;

import lombok.Getter;
import site.yuanshen.data.entity.SysRole;

/**
 *
 */
@Getter
public enum RoleEnum {

    ADMIN("ADMIN", "系统管理员", 2,1+2+4, 1),
    MAP_MANAGER("MAP_MANAGER", "地图管理者", 3,1+2+4, 2),
    MAP_NEIGUI("MAP_NEIGUI", "测试打点员", 4,1+2+4, 6),
    MAP_PUNCTUATE("MAP_PUNCTUATE", "地图打点员", 5,1+2, 3),
    MAP_USER("MAP_USER", "地图用户", 6,1, 4),
    VISITOR("VISITOR", "匿名用户", 100,1, 5),
    ;

    /**
     * 角色代码
     */
    private final String code;
    /**
     * 角色名称
     */
    private final String name;
    /**
     * 角色权限等级（数值越小，权限越大）
     */
    private final int sort;
    /**
     * 用户数据等级（二进制111分别表示0,1,2级hiddenFlag的可见性）
     */
    private final int userDataLevel;
    /**
     * 角色ID（对应数据库中的id）
     */
    private final long id;

    RoleEnum(String code, String name, int sort, int userDataLevel, int id) {
        this.code = code;
        this.name = name;
        this.sort = sort;
        this.userDataLevel = userDataLevel;
        this.id = id;
    }

    public SysRole getRoleBean() {
        SysRole roleBean = new SysRole();
        roleBean.setId(id);
        roleBean.setName(name);
        roleBean.setCode(code);
        roleBean.setSort(sort);
        return roleBean;
    }

    public static RoleEnum getRoleFromId(long id) {
        for (RoleEnum role : RoleEnum.values())
            if (role.id == id)
                return role;
        throw new RuntimeException("id无法匹配角色");
    }

    @Override
    public String toString() {
        return code;
    }
}
