package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色关联Vo
 *
 * @author Moment
 */
@Data
@Schema(title = "角色关联Vo", description = "角色关联Vo")
public class SysRoleLinkVo {

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 角色ID
     */
    @Schema(title = "角色ID")
    private Long roleId;

}
