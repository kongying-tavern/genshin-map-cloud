package site.yuanshen.data.enums;

/**
 * 打点状态枚举
 *
 * @author Moment
 */
public enum PunctuateStatusEnum {

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

    public static PunctuateStatusEnum from(int typeCode) {
        try {
            return values()[typeCode];
        } catch (Exception e) {
            throw new RuntimeException("无效的打点状态，请联系管理员");
        }
    }
}
