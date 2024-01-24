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
 * 存档槽位VO
 *
 * @author Moment
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "存档槽位VO", description = "存档槽位VO")
public class SysArchiveSlotVo {

    /**
     * 乐观锁：修改次数
     */
    private Long version;

    /**
     * 存档ID
     */
    @Schema(title = "存档ID")
    private Long id;

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
     * 创建时间
     */
    @Schema(title = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createTime;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 存档列表
     */
    @Schema(title = "存档列表")
    private List<SysArchiveVo> archive;

}
