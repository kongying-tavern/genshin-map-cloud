package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.enums.ScoreScopeEnum;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.adapter.score.ScoreDataPackVo;
import site.yuanshen.data.vo.adapter.score.ScoreDataPunctuateVo;
import site.yuanshen.data.vo.adapter.score.ScoreParamsVo;
import site.yuanshen.genshin.core.service.helper.score.ScoreHelper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreDataService {
    private final ScoreHelper commonHelper;
    private final SysUserMapper sysUserMapper;

    public List<? extends ScoreDataPackVo<ScoreDataPunctuateVo>> getData(ScoreParamsVo config) {
        final String scope = config.getScope();
        final ScoreSpanConfigDto span = config.calculateSpan();
        List<? extends ScoreDataPackVo<ScoreDataPunctuateVo>> list = new ArrayList<>();

        if(ScoreScopeEnum.PUNCTUATE.name().equals(scope))
            list = this.getDataPunctuate(span);

        packDataList(list);
        return list;
    }

    private void packDataList(List<? extends ScoreDataPackVo<ScoreDataPunctuateVo>> list) {
        UserAppenderService.appendUser(list, ScoreDataPackVo::getUserId, ScoreDataPackVo::getUserId, ScoreDataPackVo::setUser);
    }

    private List<ScoreDataPackVo<ScoreDataPunctuateVo>> getDataPunctuate(ScoreSpanConfigDto span) {
        final String scope = ScoreScopeEnum.PUNCTUATE.name();
        final List<ScoreStat> score = commonHelper.getData(scope, span);
        final Map<Long, ScoreDataPackVo<ScoreDataPunctuateVo>> scoreDataMap = new HashMap<>();
        score.forEach(scoreRow -> {
            final Long userId = scoreRow.getUserId();
            scoreDataMap.putIfAbsent(userId, (new ScoreDataPackVo<ScoreDataPunctuateVo>())
                    .setScope(scope)
                    .setSpan(span.getSpan().name())
                    .setUserId(userId)
                    .setData(new ScoreDataPunctuateVo())
            );
            final ScoreDataPunctuateVo scoreContent = JSONObject.parseObject(JSON.toJSONString(scoreRow.getContent()), ScoreDataPunctuateVo.class);
            scoreDataMap.get(userId).getData().merge(scoreContent);
        });

        return new ArrayList<>(scoreDataMap.values());
    }
}
