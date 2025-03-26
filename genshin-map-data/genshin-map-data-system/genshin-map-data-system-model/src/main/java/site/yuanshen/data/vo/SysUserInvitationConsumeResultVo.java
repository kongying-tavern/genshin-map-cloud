package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户邀请消费结果数据封装", description = "用户邀请消费结果数据封装")
public class SysUserInvitationConsumeResultVo {
    @RequiredArgsConstructor
    public enum Status {
        EXISTING,
        SUCCESS,
    }

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 账号状态
     */
    @Schema(title = "条件结果")
    private Status result;

}
