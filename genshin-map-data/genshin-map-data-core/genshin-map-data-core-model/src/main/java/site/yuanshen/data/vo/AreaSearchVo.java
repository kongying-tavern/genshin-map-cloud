package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 地区查询前端封装
 *
 * @author Moment
 * @since 2022-06-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "地区查询前端封装", description = "地区查询前端封装")
public class AreaSearchVo {

    /**
     * 父级ID
     */
    @Schema(title = "父级ID")
    private Long parentId;

    /**
     * 创建者ID
     */
    @Schema(title = "是否遍历子地区")
    private Boolean isTraverse;

}
