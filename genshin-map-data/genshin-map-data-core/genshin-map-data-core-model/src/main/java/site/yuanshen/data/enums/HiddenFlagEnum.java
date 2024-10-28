package site.yuanshen.data.enums;

import cn.hutool.core.convert.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.BiFunction;

/**
 * hidden_flag 枚举类
 */
@AllArgsConstructor
public enum HiddenFlagEnum {
    NORMAL(
        0, "正常点位",
        1
    ),
    INVISIBLE(
        1, "隐藏点位",
        1|2|4
    ),
    TEST(
        2, "内鬼点位",
        1|2|4|8
    ),
    EASTER_EGG(
        3, "彩蛋点位",
        1|8
    );

    @Getter
    private final Integer code;
    @Getter
    private final String msg;
    @Getter
    private final Integer canOverrideMask;

    /**
     * 根据掩码过滤
     */
    public static List<Integer> getFlagListByMask(int userDataLevel) {
        List<Integer> list = new ArrayList<>();
        for (HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
            if ((userDataLevel & 1 << flagEnum.code) > 0) {
                list.add(flagEnum.code);
            }
        }
        return list.isEmpty() ? Collections.singletonList(0) : list;
    }

    public static List<Integer> getFlagListByMask(String userDataLevel){
       return getFlagListByMask(Convert.toInt(userDataLevel, 0));
    }

    /**
     * 根据位置获取
     */
    public static HiddenFlagEnum getFlagListByPos(int pos) {
        HiddenFlagEnum found = null;
        for (HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
            if(flagEnum.code != null && flagEnum.code.equals(pos)) {
                found = flagEnum;
                break;
            }
        }
        return found;
    }

    /**
     * 获取点位可覆盖权限
     */
    public static Set<Integer> getOverrideFlagList(int hiddenFlag) {
        HiddenFlagEnum enumFound = getFlagListByPos(hiddenFlag);

        Set<Integer> flagSet = new HashSet<>();
        if (enumFound == null)
            return flagSet;

        for (HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
            if ((enumFound.canOverrideMask & 1 << flagEnum.code) > 0) {
                flagSet.add(flagEnum.code);
            }
        }
        return flagSet;
    }

    public static List<Integer> getAllFlagList() {
        List<Integer> list = new ArrayList<>();
        for (HiddenFlagEnum value : HiddenFlagEnum.values()) {
            list.add(value.getCode());
        }
        return list;
    }

}
