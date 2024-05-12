package site.yuanshen.data.dto.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.yuanshen.data.enums.LogicEnum;

@AllArgsConstructor
@Data
public class BoolLogicPair {
    private Boolean boolValue;
    private LogicEnum logic;
    private Boolean truncated = false;

    public static BoolLogicPair create(boolean boolValue, LogicEnum logic, boolean truncated) {
        return new BoolLogicPair(boolValue, logic, truncated);
    }
}
