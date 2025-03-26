package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import java.util.List;

/**
* 系统用户邀请表;系统用户邀请前端封装
*
* @since 2025-03-26 07:46:02
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserInvitation前端封装", description = "系统用户邀请表;系统用户邀请前端封装")
public class SysUserInvitationVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 创建人
     */
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

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
     * 角色ID
     */
    @Schema(title = "角色ID")
    private Integer roleId;

    /**
     * 备注
     */
    @Schema(title = "备注")
    private String remark;

    /**
     * 权限策略
     */
    @Schema(title = "权限策略")
    private List<String> accessPolicy;

}