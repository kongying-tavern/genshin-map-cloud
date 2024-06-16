package site.yuanshen.genshin.core.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.common.web.response.WUtils;
import site.yuanshen.genshin.core.websocket.WebSocketEntrypoint;

/**
 * 应用 Controller 层
 *
 * @author Alex Fang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
@Tag(name = "app", description = "应用API")
public class AppController {

    private final WebSocketEntrypoint webSocket;

    @Operation(summary = "触发应用更新", description = "触发应用更新")
    @PostMapping("/trigger/update")
    public R<Boolean> triggerAppUpdate() {
        webSocket.broadcast(WUtils.create("AppUpdated", null));
        return RUtils.create(true);
    }

}
