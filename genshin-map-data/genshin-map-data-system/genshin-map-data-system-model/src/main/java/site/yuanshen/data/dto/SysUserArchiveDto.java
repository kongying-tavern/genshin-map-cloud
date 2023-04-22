package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUserArchive;
import site.yuanshen.data.vo.SysUserArchiveVo;
import java.time.LocalDateTime;


/**
 * 系统用户存档表路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserArchive数据封装", description = "系统用户存档表数据封装")
public class SysUserArchiveDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 存档名称
     */
    private String name;

    /**
     * 槽位顺序
     */
    private Integer slotIndex;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 存档信息
     */
    private String data;

    public SysUserArchiveDto(SysUserArchive sysUserArchive) {
        BeanUtils.copy(sysUserArchive, this);
    }

    public SysUserArchiveDto(SysUserArchiveVo sysUserArchiveVo) {
        BeanUtils.copy(sysUserArchiveVo, this);
    }

    @JSONField(serialize = false)
    public SysUserArchive getEntity() {
        return BeanUtils.copy(this, SysUserArchive.class);
    }

    @JSONField(serialize = false)
    public SysUserArchiveVo getVo() {
        return BeanUtils.copy(this, SysUserArchiveVo.class);
    }

}