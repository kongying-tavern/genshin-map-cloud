package site.yuanshen.data.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.vo.HistorySearchVo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "历史记录分页查询数据封装", description = "历史记录分页查询数据封装")
public class HistorySearchDto extends PageSearchDto {

    /**
     * 记录类型,不传时默认查询全部类型
     */
    @Schema(title = "操作数据类型;1地区; 2图标; 3物品; 4点位; 5标签")
    private Integer type;

    /**
     * 记录类型,不传时默认查询全部数据
     */
    @Schema(title = "原ID(配合操作数据类型使用)")
    private List<Integer> id = new ArrayList<>();


    public HistorySearchDto(HistorySearchVo historySearchVo) {
        BeanUtils.copyNotNull(historySearchVo, this);
    }

    public Page<History> getPageEntity() {
        return super.getPageEntity();
    }
}
