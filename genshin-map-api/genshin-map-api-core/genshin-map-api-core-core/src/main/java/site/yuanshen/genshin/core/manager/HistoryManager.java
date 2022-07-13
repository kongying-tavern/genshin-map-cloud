package site.yuanshen.genshin.core.manager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryType;
import site.yuanshen.genshin.core.service.HistoryService;

import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class HistoryManager {
    private final HistoryService historyService;
    //private final AreaService areaService;

    public Boolean rollback(Long id) {
        History history = historyService.getById(id);
        if (Objects.isNull(history)) {
            throw new RuntimeException("history: " + id + " not exist");
        }

        HistoryType historyType = HistoryType.from(history.getType());

        switch (historyType) {
            case AREA:
                //Area area = (Area) HistoryConvert.reConvert(history, historyType);
                //return areaService.update(area);

            default:
                throw new RuntimeException("unknown history type " + history);
        }
    }
}
