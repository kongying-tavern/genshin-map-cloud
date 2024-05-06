package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 用户设备前端封装
*
* @since 2024-05-07 12:00:09
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "UserDevice前端封装", description = "用户设备前端封装")
public class UserDeviceVo {

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
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 设备编码
     */
    @Schema(title = "设备编码")
    private String deviceId;

    /**
     * IPv4
     */
    @Schema(title = "IPv4")
    private String ipv4;

}