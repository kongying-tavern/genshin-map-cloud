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
@Schema(title = "rttCheck前端封装", description = "延迟检测前端封装")
public class RttCheckVo {
    private String id = "";
    private Long receiveTimestamp;
    private Long sendTimestamp;
}
