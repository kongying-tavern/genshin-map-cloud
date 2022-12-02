package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 系统用户存档表
 *
 * @author Moment
 * @since 2022-12-03 01:12:29
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_archive")
public class SysUserArchive extends BaseEntity {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存档名称
     */
    @TableField("name")
    private String name;

    /**
     * 槽位顺序
     */
    @TableField("slot_index")
    private Integer slotIndex;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 存档信息
     */
    @TableField("data")
    private String data;

}
