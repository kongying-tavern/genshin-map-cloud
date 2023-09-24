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
@Schema(title = "公告查询前端封装", description = "公告查询前端封装")
public class NoticeSearchVo {
    /**
     * 频道
     */
    @Schema(title = "频道")
    private String channel;

    /**
     * 标题
     */
    @Schema(title = "标题")
    private String title;

    /**
     * 获取有效数据
     */
    @Schema(title = "获取有效数据")
    private Boolean getValid;

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size;
}
