package site.yuanshen.data.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.vo.HistorySearchVo;

import java.util.ArrayList;
import java.util.List;

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
    private List<Long> id = new ArrayList<>();

    /**
     * 操作数据类型
     */
    @Schema(title = "操作数据类型")
    private HistoryEditType editType;

    /**
     * 创建人ID
     */
    @Schema(title = "创建人ID")
    private Long creatorId;

    /**`
     * 排序条件
     */
    @Schema(title = "排序条件")
    private List<String> sort = new ArrayList<>();

    public HistorySearchDto(HistorySearchVo historySearchVo) {
        BeanUtils.copyNotNull(historySearchVo, this);
    }

    public Page<History> getPageEntity() {
        return super.getPageEntity();
    }
}
