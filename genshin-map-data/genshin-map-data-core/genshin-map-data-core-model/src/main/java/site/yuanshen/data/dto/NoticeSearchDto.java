package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Notice;
import site.yuanshen.data.vo.NoticeSearchVo;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "公告分页查询数据封装", description = "公告分页查询数据封装")
public class NoticeSearchDto extends PageSearchDto {
    /**
     * 频道
     */
    private List<String> channels;

    /**
     * 标题
     */
    private String title;

    /**
     * 获取有效数据
     */
    private Boolean getValid;

    /**
     * 数据转换器
     */
    private String transformer;

    /**
     * 排序条件
     */
    private List<String> sort = new ArrayList<>();

    public NoticeSearchDto(NoticeSearchVo iconSearchVo) {
        BeanUtils.copyNotNull(iconSearchVo, this);
    }

    public Page<Notice> getPageEntity() {
        return super.getPageEntity();
    }
}
