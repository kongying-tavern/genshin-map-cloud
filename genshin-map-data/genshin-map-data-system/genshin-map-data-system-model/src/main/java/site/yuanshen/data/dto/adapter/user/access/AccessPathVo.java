package site.yuanshen.data.dto.adapter.user.access;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Schema(title = "登录权限路径前端封装")
public class AccessPathVo {
    @Schema(title = "策略标记")
    private String policy;

    @Schema(title = "此策略是否通过")
    private boolean passed;
}
