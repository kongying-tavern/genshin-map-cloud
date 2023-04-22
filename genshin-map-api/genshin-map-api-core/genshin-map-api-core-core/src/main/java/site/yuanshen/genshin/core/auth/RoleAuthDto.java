package site.yuanshen.genshin.core.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.enums.RoleEnum;


/**
 * 角色数据封装
 *
 * @since 2023-04-22 04:19:03
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "角色数据封装", description = "角色数据封装")
public class RoleAuthDto implements GrantedAuthority {

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

    public RoleAuthDto(RoleEnum roleEnum) {
        BeanUtils.copy(roleEnum, this);
    }

    @Override
    public String getAuthority() {
        return code;
    }
}