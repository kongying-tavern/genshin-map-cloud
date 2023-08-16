package site.yuanshen.data.enums;

import lombok.Getter;

/**
 *
 */
@Getter
public enum RoleEnum {

    ADMIN("ADMIN", "系统管理员", 2,1|2|4|8),
    MAP_MANAGER("MAP_MANAGER", "地图管理者", 3,1|2|4|8),
    MAP_NEIGUI("MAP_NEIGUI", "测试打点员", 4,1|2|4|8),
    MAP_PUNCTUATE("MAP_PUNCTUATE", "地图打点员", 5,1|2|8),
    MAP_USER("MAP_USER", "地图用户", 6,1|8),
    VISITOR("VISITOR", "匿名用户", 100,1|8),
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

    RoleEnum(String code, String name, int sort, int userDataLevel) {
        this.code = code;
        this.name = name;
        this.sort = sort;
        this.userDataLevel = userDataLevel;
    }


    public static RoleEnum getRoleFromId(int id) {
        if (id < 0 || id >= RoleEnum.values().length) {
            throw new IllegalArgumentException("角色ID错误");
        }
        return RoleEnum.values()[id];
    }

    public static RoleEnum getRoleFromCode(String code) {
        for (RoleEnum e : RoleEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("角色代码不存在");
    }


    @Override
    public String toString() {
        return code;
    }
}
