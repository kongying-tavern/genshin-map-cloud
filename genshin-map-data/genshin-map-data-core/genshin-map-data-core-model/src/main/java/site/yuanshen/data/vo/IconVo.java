package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
* 图标主表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Icon前端封装", description = "图标主表前端封装")
public class IconVo {

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
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 图标名称
     */
    @Schema(title = "图标名称")
    private String name;

    /**
     * 图标类型ID列表
     */
    @Schema(title = "图标类型ID列表")
    private List<Long> typeIdList;

    /**
     * 图标url
     */
    @Schema(title = "图标url")
    private String url;

}