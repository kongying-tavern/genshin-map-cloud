package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点位关联查询前端封装
 *
 * @author Alex Fang
 * @since 2022-10-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "点位关联查询前端封装", description = "点位关联查询前端封装")
public class MarkerLinkageSearchVo {

    private List<String> groupIds;
}
