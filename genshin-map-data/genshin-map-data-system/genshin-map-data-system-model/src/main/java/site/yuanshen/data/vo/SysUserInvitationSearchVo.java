package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "用户邀请查询前端封装", description = "用户邀请查询前端封装")
public class SysUserInvitationSearchVo {
    /**
     * 当前页，从1开始
     */
    @Schema(title = "当前页，从1开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    /**
     * 邀请码
     */
    @Schema(title = "邀请码")
    private String code;

    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;

    /**
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort;
}
