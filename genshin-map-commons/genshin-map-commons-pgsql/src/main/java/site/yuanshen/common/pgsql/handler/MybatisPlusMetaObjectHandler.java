package site.yuanshen.common.pgsql.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * mybatis-plus的拦截器
 * 用于updateTime与createTime的自动生成
 *
 * @author Alex
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        log.debug("auto insert fill: createTime, updateTime : " + date);
        this.setFieldValByName("createTime", date, metaObject);
        this.setFieldValByName("updateTime", date, metaObject);
        this.setFieldValByName("version", 1L, metaObject);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        String userId = requestAttributes.getRequest().getHeader("userId");
        if (userId == null || userId.equals("")) return;
        this.setFieldValByName("creatorId", Long.parseLong(userId), metaObject);
        this.setFieldValByName("updaterId", Long.parseLong(userId), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        log.debug("auto insert fill: createTime, updateTime : " + date);
        this.setFieldValByName("updateTime", date, metaObject);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        String userId = requestAttributes.getRequest().getHeader("userId");
        if (userId == null || userId.equals("")) return;
        this.setFieldValByName("updaterId", Long.parseLong(userId), metaObject);
    }
}
