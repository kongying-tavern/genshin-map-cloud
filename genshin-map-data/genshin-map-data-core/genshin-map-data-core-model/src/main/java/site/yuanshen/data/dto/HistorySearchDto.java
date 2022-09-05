package site.yuanshen.data.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.vo.HistorySearchVo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(title = "历史记录分页查询数据封装", description = "历史记录分页查询数据封装")
public class HistorySearchDto {

    /**
     * 当前页，从0开始
     */
    @Schema(title = "当前页，从0开始")
    private Long current = 0L;;

    /**
     * 每页大小，默认为10
     */
    @Schema(title = "每页大小，默认为10")
    private Long size = 10L;;

    /**
     * 记录类型,不传时默认查询全部类型
     */
    @Schema(title = "记录类型")
    private Integer type;

    /**
     * 记录类型,不传时默认查询全部数据
     */
    @Schema(title = "类型ID(配合记录类型使用)")
    private List<Integer> id = new ArrayList<>();


    public HistorySearchDto(HistorySearchVo historySearchVo) {
        BeanUtils.copyNotNull(historySearchVo, this);
    }

    public Page<History> getPageEntity() {
        return new Page<>(current, size);
    }
}
