package site.yuanshen.common.web.feign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import site.yuanshen.common.web.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Feign的传参拦截器 - 自定义的给Feign请求中添加相应的一些参数
 *
 * @author: Moment
 */
public class FeignParamInterceptor implements RequestInterceptor {

    /**
     * 请求参数名列表
     */
    @Autowired
    private FeignParamterProperties feignParamterProperties;

    @Override
    public void apply(RequestTemplate template) {
        //获取传递的参数列表
        List<String> paramNames = feignParamterProperties.getParamsName();
        //非空校验
        if (paramNames == null || paramNames.size() == 0) return;

        //获取需要传递的参数
        HttpServletRequest httpServletRequest = RequestUtils.getHttpServletRequest();

        for (String paramName : paramNames) {
            //根据参数名获取请求头中的参数值
            String headerValue = httpServletRequest.getHeader(paramName);
            if (!StringUtils.isEmpty(headerValue)) {
                //获取到参数值，将参数值继续放入到Feign请求的请求头中
                template.header(paramName, headerValue);
            }

            //根据参数名获取请求参数列表中的参数值
            String paramValue = httpServletRequest.getParameter(paramName);
            if (!StringUtils.isEmpty(paramValue)) {
                //继续往后传递
                template.query(paramName, paramValue);
            }
        }

    }
}
