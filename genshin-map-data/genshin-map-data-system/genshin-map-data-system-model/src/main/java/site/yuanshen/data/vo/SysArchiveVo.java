package site.yuanshen.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.sql.Timestamp;

/**
 * 存档VO
 *
 * @author Moment
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "存档VO", description = "存档VO")
public class SysArchiveVo {

    /**
     * 存档时间
     */
    @Schema(title = "存档时间")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp time;

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
