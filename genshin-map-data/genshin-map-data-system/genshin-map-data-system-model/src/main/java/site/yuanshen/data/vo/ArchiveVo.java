package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存档VO
 *
 * @author Moment
 */
@Data
@Schema(title = "存档VO", description = "存档VO")
public class ArchiveVo {

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
    private long slotIndex;

    /**
     * 创建时间
     */
    @Schema(title = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 存档
     */
    @Schema(title = "存档")
    private String archive;

    /**
     * 存档历史下标
     */
    @Schema(title = "存档历史下标")
    private long historyIndex;

}