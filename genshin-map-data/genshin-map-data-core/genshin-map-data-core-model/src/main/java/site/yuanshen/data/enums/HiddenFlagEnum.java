package site.yuanshen.data.enums;

import cn.hutool.core.convert.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * hidden_flag 枚举类
 */
@AllArgsConstructor
public enum HiddenFlagEnum {
    NORMAL(0,"正常点位"),
    INVISIBLE(1,"隐藏点位"),
    TEST(2,"内鬼点位")

    ;

    @Getter
    private final Integer code;
    @Getter
    private final String msg;

    /**
     * 判断点位权限
     */
    public static List<Integer> getFlagList(int userDataLevel){
        List<Integer> list = new ArrayList<>();
        for (HiddenFlagEnum flagEnum:HiddenFlagEnum.values()){
//            log.info("权限判断:"+(userDataLevel&1<<flagEnum.code));
            if ((userDataLevel&1<<flagEnum.code)>0){
                list.add(flagEnum.code);
            }
        }
        return list.isEmpty()? Collections.singletonList(0):list;
    }

    public static List<Integer> getFlagList(String userDataLevel){
       return getFlagList(Convert.toInt(userDataLevel,0));
    }

    public static List<Integer> getAllFlagList() {
        List<Integer> list = new ArrayList<>();
        for (HiddenFlagEnum value : HiddenFlagEnum.values()) {
            list.add(value.getCode());
        }
        return list;
    }

}
