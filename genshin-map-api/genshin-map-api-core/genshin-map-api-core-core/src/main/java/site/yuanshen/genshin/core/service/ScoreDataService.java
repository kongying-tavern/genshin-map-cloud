package site.yuanshen.genshin.core.service;

import site.yuanshen.data.vo.adapter.score.ScoreDataPackVo;
import site.yuanshen.data.vo.adapter.score.ScoreParamsVo;

import java.util.List;

public interface ScoreDataService {
    List<? extends ScoreDataPackVo> getData(ScoreParamsVo span);
}
