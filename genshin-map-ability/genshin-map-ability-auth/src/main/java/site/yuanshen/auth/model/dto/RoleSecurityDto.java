package site.yuanshen.auth.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.enums.RoleEnum;

/**
 * 角色Dto
 *
 * @author Moment
 */
@Data
@NoArgsConstructor
public class RoleSecurityDto implements GrantedAuthority {

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

    @Override
    public String getAuthority() {
        return code;
    }

    public RoleSecurityDto(String code) {
        CachedBeanCopier.copyProperties(RoleEnum.valueOf(code), this);
    }
}
