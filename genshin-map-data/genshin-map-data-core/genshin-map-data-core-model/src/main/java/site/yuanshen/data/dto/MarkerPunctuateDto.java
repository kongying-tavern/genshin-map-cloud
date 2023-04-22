package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerPunctuate;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import java.time.LocalDateTime;


/**
 * 点位提交表路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarkerPunctuate数据封装", description = "点位提交表数据封装")
public class MarkerPunctuateDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 点位提交ID
     */
    private Long punctuateId;

    /**
     * 原有点位ID
     */
    private Long originalMarkerId;

    /**
     * 点位名称
     */
    private String markerTitle;

    /**
     * 点位物品列表
     */
    private String itemList;

    /**
     * 点位坐标
     */
    private String position;

    /**
     * 点位说明
     */
    private String content;

    /**
     * 额外特殊字段
     */
    private String extra;

    /**
     * 点位图片
     */
    private String picture;

    /**
     * 点位初始标记者
     */
    private Long markerCreatorId;

    /**
     * 点位图片上传者
     */
    private Long pictureCreatorId;

    /**
     * 点位视频
     */
    private String videoPath;

    /**
     * 隐藏标志
     */
    private Integer hiddenFlag;

    /**
     * 点位提交者ID
     */
    private Long author;

    /**
     * 状态;0:暂存 1:审核中 2:不通过
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 操作类型;1: 新增 2: 修改 3: 删除
     */
    private Integer methodType;

    /**
     * 点位刷新时间
     */
    private Long refreshTime;

    public MarkerPunctuateDto(MarkerPunctuate markerPunctuate) {
        BeanUtils.copy(markerPunctuate, this);
    }

    public MarkerPunctuateDto(MarkerPunctuateVo markerPunctuateVo) {
        BeanUtils.copy(markerPunctuateVo, this);
    }

    @JSONField(serialize = false)
    public MarkerPunctuate getEntity() {
        return BeanUtils.copy(this, MarkerPunctuate.class);
    }

    @JSONField(serialize = false)
    public MarkerPunctuateVo getVo() {
        return BeanUtils.copy(this, MarkerPunctuateVo.class);
    }

}