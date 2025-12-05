package site.yuanshen.genshin.core.socketIO;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@AutoConfigureAfter({SocketIOConfiguration.class})
@Slf4j
public class SocketIOStarter implements CommandLineRunner, DisposableBean {

    @Resource
    private SocketIOServer socketIOServer;

    @Override
    public void run(String... args) {
        socketIOServer.start();
    }

    @Override
    public void destroy() {
        socketIOServer.stop();
    }
}
