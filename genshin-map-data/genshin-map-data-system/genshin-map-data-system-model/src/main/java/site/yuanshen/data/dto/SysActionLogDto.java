package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysActionLog;
import site.yuanshen.data.vo.SysActionLogVo;

import java.sql.Timestamp;
import java.util.Map;


/**
 * 系统操作日志表;系统操作日志数据封装
 *
 * @since 2024-06-04 05:15:23
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysActionLog数据封装", description = "系统操作日志表;系统操作日志数据封装")
public class SysActionLogDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Long updaterId;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * IPv4
     */
    private String ipv4;

    /**
     * 设备编码
     */
    private String deviceId;

    /**
     * 操作名
     */
    private String action;

    /**
     * 是否是错误
     */
    private Boolean isError;

    /**
     * JSON对象
     */
    private Map<String, Object> extraData;

    public SysActionLogDto(SysActionLog sysActionLog) {
        BeanUtils.copy(sysActionLog, this);
    }

    public SysActionLogDto(SysActionLogVo sysActionLogVo) {
        BeanUtils.copy(sysActionLogVo, this);
    }

    @JSONField(serialize = false)
    public SysActionLog getEntity() {
        return BeanUtils.copy(this, SysActionLog.class);
    }

    @JSONField(serialize = false)
    public SysActionLogVo getVo() {
        return BeanUtils.copy(this, SysActionLogVo.class);
    }

}