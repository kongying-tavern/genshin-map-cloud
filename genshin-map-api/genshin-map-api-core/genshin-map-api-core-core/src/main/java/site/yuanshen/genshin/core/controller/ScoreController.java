package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.adapter.score.ScoreGenerateVo;
import site.yuanshen.genshin.core.service.ScoreGenerateService;

/**
 * 评分统计 Controller 层
 *
 * @author Alex Fang
 * @since 2023-01-20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/score")
@Tag(name = "score", description = "评分统计API")
public class ScoreController {
    private final ScoreGenerateService scoreGenerateService;

    @Operation(summary = "生成评分", description = "生成评分数据")
    @PostMapping("/generate")
    public R<Object> generate(@RequestBody ScoreGenerateVo scoreGenerateVo, @RequestHeader("userId") Long userId) {
        scoreGenerateVo.setGeneratorId(userId);
        scoreGenerateService.generateScore(scoreGenerateVo);
        return RUtils.create("ok");
    }
}
