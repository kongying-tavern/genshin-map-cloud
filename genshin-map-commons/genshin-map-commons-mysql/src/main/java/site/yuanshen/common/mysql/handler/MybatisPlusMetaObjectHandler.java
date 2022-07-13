package site.yuanshen.common.mysql.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * mybatis-plus的拦截器
 * 用于updatedTime与createdTime的自动生成
 *
 * @author Moment
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        log.debug("auto insert fill: createdTime, updatedTime : " + date);
        this.setFieldValByName("createTime", date, metaObject);
        this.setFieldValByName("updateTime", date, metaObject);
        this.setFieldValByName("revision", 1, metaObject);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        String userId = requestAttributes.getRequest().getHeader("userId");
        if (userId == null || userId.equals("")) return;
        this.setFieldValByName("creatorId", userId, metaObject);
        this.setFieldValByName("creatorId", userId, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        log.debug("auto insert fill: createdTime, updatedTime : " + date);
        this.setFieldValByName("updatedTime", date, metaObject);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        String userId = requestAttributes.getRequest().getHeader("userId");
        if (userId == null || userId.equals("")) return;
        this.setFieldValByName("creatorId", userId, metaObject);
    }
}
