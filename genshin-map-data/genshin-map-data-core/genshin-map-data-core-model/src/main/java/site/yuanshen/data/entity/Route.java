package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 路线
 *
 * @author Moment
 * @since 2023-01-03 04:31:56
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode(callSuper = true)
@TableName("route")
public class Route extends BaseEntity {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 路线名称
     */
    @TableField("name")
    private String name;

    /**
     * 路线描述
     */
    @TableField("content")
    private String content;

    /**
     * 点位顺序数组
     */
    @TableField("marker_list")
    private String markerList;

    /**
     * 显隐等级
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 视频地址
     */
    @TableField("video")
    private String video;

    /**
     * 额外信息
     */
    @TableField("extra")
    private String extra;

    /**
     * 创建人昵称
     */
    @TableField("creator_nickname")
    private String creatorNickname;


}
