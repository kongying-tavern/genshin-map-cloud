package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

/**
* 系统用户存档表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SysUserArchive前端封装", description = "系统用户存档表前端封装")
public class SysUserArchiveVo {

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
     * 存档名称
     */
    @Schema(title = "存档名称")
    private String name;

    /**
     * 槽位顺序
     */
    @Schema(title = "槽位顺序")
    private Integer slotIndex;

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 存档信息
     */
    @Schema(title = "存档信息")
    private List<Object> data;

}
