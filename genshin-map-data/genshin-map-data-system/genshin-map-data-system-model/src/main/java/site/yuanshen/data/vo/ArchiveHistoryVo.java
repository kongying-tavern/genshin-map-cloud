package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存档历史VO
 *
 * @author Moment
 */
@Data
@Schema(title = "存档历史VO", description = "存档历史VO")
public class ArchiveHistoryVo {

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
    private Long slotIndex;

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
     * 存档列表
     */
    @Schema(title = "存档列表")
    private String[] archive;

    /**
     * 存档历史下标
     */
    @Schema(title = "存档历史下标")
    private Long historyIndex;

}