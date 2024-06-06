package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysActionLog;
import site.yuanshen.data.vo.SysActionLogSearchVo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "操作日志分页查询数据封装", description = "操作日志分页查询数据封装")
public class SysActionLogSearchDto {

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;

    /**
     * 用户名
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * IP地址
     */
    @Schema(title = "IPv4")
    private String ipv4;

    /**
     * 昵称
     */
    @Schema(title = "设备ID")
    private String deviceId;

    /**
     * 操作名
     */
    @Schema(title = "操作名")
    private String action;

    /**
     * 是否是错误
     */
    @Schema(title = "是否是错误")
    private Boolean isError;

    /**
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort;

    public SysActionLogSearchDto(SysActionLogSearchVo sysActionLogSearchVo){
        BeanUtils.copyNotNull(sysActionLogSearchVo, this);
    }

    @JSONField(serialize = false)
    public Page<SysActionLog> getPageEntity(){
        return new Page<>(current, size);
    }
}
