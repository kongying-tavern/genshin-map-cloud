package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import site.yuanshen.genshin.core.service.ScoreDataService;
import site.yuanshen.genshin.core.service.impl.helper.score.ScoreHelper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreDataServiceImpl implements ScoreDataService {
    private final ScoreHelper commonHelper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<? extends ScoreDataPackVo> getData(ScoreParamsVo config) {
        final String scope = config.getScope();
        final ScoreSpanConfigDto span = config.calculateSpan();
        List<? extends ScoreDataPackVo> list = new ArrayList();

        if(ScoreScopeEnum.PUNCTUATE.name().equals(scope))
            list = this.getDataPunctuate(span);

        packDataList(list);
        return list;
    }

    private void packDataList(List<? extends ScoreDataPackVo> list) {
        final List<Long> userIds = list.stream()
                .filter(Objects::nonNull)
                .map(ScoreDataPackVo::getUserId)
                .collect(Collectors.toList());
        final List<SysUser> userList = sysUserMapper.selectUserWithDelete(userIds);
        final Map<Long, SysUserVo> userVos = userList.stream()
                .filter(Objects::nonNull)
                .map(o -> (new SysUserDto(o)).getVo())
                .collect(Collectors.toMap(
                        SysUserVo::getId,
                        v -> v,
                        (o, n) -> n
                ));

        for(ScoreDataPackVo item : list) {
            final Long userId = item.getUserId();
            final SysUserVo user = userVos.getOrDefault(userId, new SysUserVo());
            item.setUser(user);
        }
    }

    private List<ScoreDataPackVo<ScoreDataPunctuateVo>> getDataPunctuate(ScoreSpanConfigDto span) {
        final String scope = ScoreScopeEnum.PUNCTUATE.name();
        final List<ScoreStat> score = commonHelper.getData(scope, span);
        final Map<Long, ScoreDataPackVo<ScoreDataPunctuateVo>> scoreDataMap = new HashMap<>();
        score.forEach(scoreRow -> {
            final Long userId = scoreRow.getUserId();
            scoreDataMap.putIfAbsent(userId, (new ScoreDataPackVo())
                    .setScope(scope)
                    .setSpan(span.getSpan().name())
                    .setUserId(userId)
                    .setData(new ScoreDataPunctuateVo())
            );
            final ScoreDataPunctuateVo scoreContent = JSONObject.parseObject(JSON.toJSONString(scoreRow.getContent()), ScoreDataPunctuateVo.class);
            scoreDataMap.get(userId).getData().merge(scoreContent);
        });

        return scoreDataMap.values().stream().collect(Collectors.toList());
    }
}
