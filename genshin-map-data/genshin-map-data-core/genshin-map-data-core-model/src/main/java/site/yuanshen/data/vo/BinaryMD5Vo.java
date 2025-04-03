package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "归档类MD5前端封装", description = "归档类MD5前端封装")
public class BinaryMD5Vo {

    /**
     * md5
     */
    @Schema(title = "md5")
    private String md5;

    /**
     * 时间戳
     */
    @Schema(title = "时间戳")
    private Long time;

}
