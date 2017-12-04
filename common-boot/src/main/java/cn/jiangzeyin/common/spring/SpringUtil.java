package cn.jiangzeyin.common.spring;

import cn.jiangzeyin.CommonPropertiesFinal;
import cn.jiangzeyin.common.BaseApplication;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.SystemInitPackageControl;
import cn.jiangzeyin.pool.ThreadPoolService;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * @author jiangzeyin
 * Created by jiangzeyin on 2017/1/5.
 */
@Configuration
public class SpringUtil implements ApplicationListener, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private ApplicationEventClient applicationEventClient;

    /**
     * 容器加载完成
     *
     * @param applicationContext application
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
        DefaultSystemLog.init();
        applicationEventClient = BaseApplication.getApplicationEventClient();
        if (applicationEventClient != null)
            applicationEventClient.applicationLoad();
    }

    /**
     * 启动完成
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (applicationEventClient != null)
            applicationEventClient.onApplicationEvent(event);
        if (event instanceof ApplicationReadyEvent) {// 启动最后的预加载
            SystemInitPackageControl.init();
            DefaultSystemLog.LOG().info("common-boot 启动完成");
            return;
        }
        if (event instanceof ContextClosedEvent) { // 应用关闭
            DefaultSystemLog.LOG().info("common-boot 关闭程序");
            ThreadPoolService.shutdown();
            return;
        }
        if (event instanceof ServletRequestHandledEvent) {
            ServletRequestHandledEvent servletRequestHandledEvent = (ServletRequestHandledEvent) event;
            if (!servletRequestHandledEvent.wasFailure()) {
                DefaultSystemLog.LOG(DefaultSystemLog.LogType.REQUEST).info(servletRequestHandledEvent.toString());
            } else {
                DefaultSystemLog.LOG(DefaultSystemLog.LogType.REQUEST).info("error:" + servletRequestHandledEvent.toString());
            }
        }
    }

    /**
     * 获取applicationContext
     *
     * @return application
     */
    public static ApplicationContext getApplicationContext() {
        Assert.notNull(applicationContext, "application is null");
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name 名称
     * @return 对象
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);

    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz class
     * @param <T>   对象
     * @return 对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name  名称
     * @param clazz class
     * @param <T>   对象
     * @return 对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 获取配置文件信息
     *
     * @return environment
     */
    public static Environment getEnvironment() {
        return BaseApplication.getEnvironment();
    }

    /**
     * 获取程序id
     *
     * @return id
     */
    public static String getApplicationId() {
        return getEnvironment().getProperty(CommonPropertiesFinal.APPLICATION_ID);
    }
}

