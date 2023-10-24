package site.yuanshen.data.vo.adapter.markerLinkage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MarkerLinkagePathEdgeVo {

    @Schema(title = "起始点位ID", description = "输出时会转换为 X1 & Y1")
    private Long id1;

    @Schema(title = "起始位置X坐标")
    private Double x1;

    @Schema(title = "起始位置Y坐标")
    private Double y1;

    @Schema(title = "起始曲线句柄X坐标", description = "起始位置的三次贝塞尔曲线句柄X坐标")
    private Double handleX1;

    @Schema(title = "起始曲线句柄Y坐标", description = "起始位置的三次贝塞尔曲线句柄Y坐标")
    private Double handleY1;

    @Schema(title = "起点箭头形状")
    private ArrowType arrowType1;

    @Schema(title = "终止点位ID", description = "输出时会转换为 X2 & Y2")
    private Long id2;

    @Schema(title = "终止位置X坐标")
    private Double x2;

    @Schema(title = "终止位置Y坐标")
    private Double y2;

    @Schema(title = "终止曲线句柄X坐标", description = "终止位置的三次贝塞尔曲线句柄X坐标")
    private Double handleX2;

    @Schema(title = "终止曲线句柄Y坐标", description = "终止位置的三次贝塞尔曲线句柄Y坐标")
    private Double handleY2;

    @Schema(title = "终点箭头形状")
    private ArrowType arrowType2;

    @Schema(title = "线条样式")
    private LineType lineType;

    public enum LineType {
        SOLID,
        DASHED
    }

    public enum ArrowType {
        NONE,
        ARROW,
        CIRCLE,
        DOT
    }
}
