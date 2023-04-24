package site.yuanshen.data.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.With;

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
     * 逻辑删除: false:未删除，true:删除
     */
    @TableLogic(value = "false", delval = "true")
    @JsonIgnore
    @Schema(title = "逻辑删除: false:未删除，true:删除")
    private Boolean delFlag;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(title = "创建人")
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
