package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.adapter.score.v1.ScoreDataPackVo;
import site.yuanshen.data.vo.adapter.score.v1.ScoreParamsVo;
import site.yuanshen.genshin.core.service.score.v1.ScoreDataV1Service;
import site.yuanshen.genshin.core.service.score.v1.ScoreGenerateV1Service;

import java.util.List;

/**
 * 评分统计 Controller 层
 *
 * @author Alex Fang
 * @since 2023-01-20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/score/v1")
@Tag(name = "scoreV1", description = "评分统计API V1版本")
public class ScoreV1Controller {
    private final ScoreGenerateV1Service scoreGenerateV1Service;
    private final ScoreDataV1Service scoreDataV1Service;

    @Operation(summary = "生成评分", description = "生成评分数据")
    @PostMapping("/generate")
    public R<Object> generate(
        @RequestBody ScoreParamsVo scoreParamsVo, @Parameter(hidden = true) @RequestHeader("userId") Long userId
    ) {
        scoreParamsVo.setGeneratorId(userId);
        scoreGenerateV1Service.generateScore(scoreParamsVo);
        return RUtils.create("ok");
    }

    @Operation(summary = "获取评分", description = "获取评分数据")
    @PostMapping("/data")
    public R<Object> getData(@RequestBody ScoreParamsVo scoreParamsVo) {
        List<? extends ScoreDataPackVo> data = scoreDataV1Service.getData(scoreParamsVo);
        return RUtils.create(data);
    }
}
