package site.yuanshen.data.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 打点状态枚举
 *
 * @author Moment
 */
public enum PunctuateStatusEnum implements IEnum<Integer> {

    /**
     * 暂存
     */
    STAGE,
    /**
     * 审核中
     */
    COMMIT,
    /**
     * 驳回
     */
    REJECT;

    @Override
    public Integer getValue() {
        return this.ordinal();
    }

    public static PunctuateStatusEnum from(Integer code) {
        return values()[code];
    }
}
