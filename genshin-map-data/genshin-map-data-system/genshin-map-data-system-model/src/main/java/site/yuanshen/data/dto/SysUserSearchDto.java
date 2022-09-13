package site.yuanshen.data.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.vo.SysUserSearchVo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "用户分页查询数据封装", description = "用户分页查询数据封装")
public class SysUserSearchDto {


    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current  = 0L;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size  = 10L;


    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;


    /**
     * 昵称
     */
    @Schema(title = "昵称")
    private String nickname;


    /**
     * 排序条件
     */
    private List<String> sort;



    public SysUserSearchDto(SysUserSearchVo sysUserSearchVo){
        BeanUtils.copyNotNull(sysUserSearchVo,this);
    }


    @JSONField(serialize = false)
    public Page<SysUser> getPageEntity(){
        return new Page<>(current,size);
    }
}
