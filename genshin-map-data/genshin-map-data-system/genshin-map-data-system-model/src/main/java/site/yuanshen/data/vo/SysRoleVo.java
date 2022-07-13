package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * TODO
 *
 * @author Moment
 */
@Data
public class SysRoleVo {

    /**
     * 角色ID
     */
    @Schema(title = "角色ID")
    private Long id;

    /**
     * 角色名
     */
    @Schema(title = "角色名")
    private String name;

    /**
     * 角色代码（英文大写）
     */
    @Schema(title = "角色代码（英文大写）")
    private String code;

    /**
     * 角色层级（越大级别越高）
     */
    @Schema(title = "角色层级（越大级别越高）")
    private Integer sort;


}
