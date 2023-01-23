package site.yuanshen.genshin.core.service;

import site.yuanshen.data.vo.adapter.score.ScoreParamsVo;

/**
 * 评分服务接口
 *
 * @author Alex Fang
 */
public interface ScoreGenerateService {
    void generateScore(ScoreParamsVo config);
}
