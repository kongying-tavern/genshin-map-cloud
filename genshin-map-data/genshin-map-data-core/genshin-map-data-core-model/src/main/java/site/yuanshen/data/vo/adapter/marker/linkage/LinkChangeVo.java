package site.yuanshen.data.vo.adapter.marker.linkage;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class LinkChangeVo implements Serializable {
    /**
     * 被影响的分组
     */
    private Set<String> groups = new HashSet<>();

    /**
     * 被影响的点位
     */
    private Set<Long> markers = new HashSet<>();

    public void addGroups(Set<String> groupIdSet) {
        this.groups.addAll(groupIdSet);
    }

    public void addGroups(List<String> groupIdList) {
        this.groups.addAll(groupIdList);
    }

    public void addMarkers(Set<Long> markerSet) {
        this.markers.addAll(markerSet);
    }

    public void addMarkers(List<Long> markerList) {
        this.markers.addAll(markerList);
    }
}
