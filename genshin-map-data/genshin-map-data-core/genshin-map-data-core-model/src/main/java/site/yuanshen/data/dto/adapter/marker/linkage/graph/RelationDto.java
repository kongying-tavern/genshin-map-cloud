package site.yuanshen.data.dto.adapter.marker.linkage.graph;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import site.yuanshen.data.enums.marker.linkage.IdTypeEnum;
import site.yuanshen.data.enums.marker.linkage.RelationTypeEnum;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.LinkRefVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.RelationVo;

import java.util.HashSet;
import java.util.Set;

@Getter
public class RelationDto {
    @Setter
    private String type = "";

    private Set<LinkRefVo> triggers = new HashSet<>();

    private Set<LinkRefVo> targets = new HashSet<>();

    private Set<LinkRefVo> group = new HashSet<>();

    public void addRelation(RelationTypeEnum relationType, IdTypeEnum idType, LinkRefDto link) {
        if (link == null) {
            return;
        }
        final LinkRefVo linkVal = link.toVo(idType);
        if(linkVal == null) {
            return;
        }
        this.addRelation(relationType, linkVal);
    }

    public void addRelation(RelationTypeEnum type, LinkRefVo link) {
        if (link == null) {
            return;
        }
        switch (type) {
            case TRIGGER:
                this.getTriggers().add(link);
                break;
            case TARGET:
                this.getTargets().add(link);
                break;
            case GROUP:
                this.getGroup().add(link);
                break;
        }
    }

    public RelationVo toVo() {
        final RelationVo vo = new RelationVo();
        vo.setType(StrUtil.blankToDefault(this.type, ""));
        if(CollUtil.isNotEmpty(this.triggers)) vo.setTriggers(this.triggers);
        if(CollUtil.isNotEmpty(this.targets)) vo.setTargets(this.targets);
        if(CollUtil.isNotEmpty(this.group)) vo.setGroup(this.group);
        return vo;
    }
}
