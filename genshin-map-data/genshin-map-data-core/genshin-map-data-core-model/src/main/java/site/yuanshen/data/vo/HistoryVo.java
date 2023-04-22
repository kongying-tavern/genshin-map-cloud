package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 历史操作表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "History前端封装", description = "历史操作表前端封装")
public class HistoryVo {

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
     * 内容
     */
    @Schema(title = "内容")
    private String content;

    /**
     * MD5
     */
    @Schema(title = "MD5")
    private String md5;

    /**
     * 原ID
     */
    @Schema(title = "原ID")
    private Long tId;

    /**
     * 操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签
     */
    @Schema(title = "操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签")
    private Integer type;

    /**
     * IPv4
     */
    @Schema(title = "IPv4")
    private String ipv4;

}