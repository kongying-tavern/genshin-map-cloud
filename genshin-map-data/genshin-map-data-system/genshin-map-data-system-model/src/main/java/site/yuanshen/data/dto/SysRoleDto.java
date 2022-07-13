package site.yuanshen.data.dto;

import lombok.Data;

/**
 * 角色Dto
 *
 * @author Moment
 */
@Data
public class SysRoleDto {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色代码（英文大写）
     */
    private String code;

    /**
     * 角色层级（越大级别越高）
     */
    private Integer sort;


}
