package site.yuanshen.data.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体类基类
 *
 * @author Moment
 */
@Data
@TableName(autoResultMap = true)
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 逻辑删除:0:未删除，1:删除
     */
    @TableLogic
    @JsonIgnore
    @Schema(title = "逻辑删除:0:未删除，1:删除")
    private String delFlag;

    /**
     * 乐观锁：修改次数
     */
    @Version
    @Schema(title = "乐观锁：修改次数")
    private Long version;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
