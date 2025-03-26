package site.yuanshen.data.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.vo.SysUserInvitationSearchVo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "用户邀请数据分页查询数据封装", description = "用户邀请数据分页查询数据封装")
public class SysUserInvitationSearchDto {
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

    public <T> Page<T> getPageEntity() {
        return new Page<>(current, size);
    }

    public SysUserInvitationSearchDto(SysUserInvitationSearchVo sysUserInvitationSearchVo){
        BeanUtils.copyNotNull(sysUserInvitationSearchVo, this);
    }
}
