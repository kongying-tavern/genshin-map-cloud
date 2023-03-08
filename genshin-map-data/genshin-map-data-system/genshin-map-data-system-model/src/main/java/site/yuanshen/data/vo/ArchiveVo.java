package site.yuanshen.data.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.With;
import lombok.experimental.WithBy;

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
     * 存档时间
     */
    @Schema(title = "存档时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 存档
     */
    @Schema(title = "存档")
    private String archive;

    /**
     * 存档历史下标
     */
    @Schema(title = "存档历史下标")
    private int historyIndex;

}