package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.adapter.score.v1.ScoreDataPackVo;
import site.yuanshen.data.vo.adapter.score.v1.ScoreParamsVo;
import site.yuanshen.genshin.core.service.ScoreDataService;
import site.yuanshen.genshin.core.service.ScoreGenerateService;

import java.util.List;

/**
 * 评分统计 Controller 层
 *
 * @author Alex Fang
 * @since 2023-01-20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/score")
@Tag(name = "score", description = "评分统计API")
public class ScoreController {
    private final ScoreGenerateService scoreGenerateService;
    private final ScoreDataService scoreDataService;

    @Operation(summary = "生成评分", description = "生成评分数据")
    @PostMapping("/generate")
    public R<Object> generate(@RequestBody ScoreParamsVo scoreParamsVo, @RequestHeader("userId") Long userId) {
        scoreParamsVo.setGeneratorId(userId);
        scoreGenerateService.generateScore(scoreParamsVo);
        return RUtils.create("ok");
    }

    @Operation(summary = "获取评分", description = "获取评分数据")
    @PostMapping("/data")
    public R<Object> getData(@RequestBody ScoreParamsVo scoreParamsVo) {
        List<? extends ScoreDataPackVo> data = scoreDataService.getData(scoreParamsVo);
        return RUtils.create(data);
    }
}
