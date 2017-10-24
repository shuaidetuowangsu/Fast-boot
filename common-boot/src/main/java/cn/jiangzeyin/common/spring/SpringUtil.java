package cn.jiangzeyin.common.spring;

import cn.jiangzeyin.CommonPropertiesFinal;
import cn.jiangzeyin.system.log.SystemLog;
import cn.jiangzeyin.util.util.PackageUtil;
import cn.jiangzeyin.util.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author jiangzeyin
 * Created by jiangzeyin on 2017/1/5.
 */
@Configuration
public class SpringUtil implements ApplicationListener, ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;
    private static Environment environment;

    /**
     * 容器加载完成
     *
     * @param applicationContext application
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
        System.out.println("加载中");
        SystemLog.init();
    }

    /**
     * 启动完成
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            //SystemInitPackageControl.init();
            //创建tomcat 临时文件
            //ServiceInfoUtil.initTomcatTemPath();
            SystemLog.LOG().info("启动完成");
            return;
        }
        if (event instanceof ContextStartedEvent) { // 应用启动，需要在代码动态添加监听器才可捕获
            System.out.println("3");
        } else if (event instanceof ContextStoppedEvent) { // 应用停止
            System.out.println("stop");
        } else if (event instanceof ContextClosedEvent) { // 应用关闭
            SystemLog.LOG().info("关闭程序");
            //SystemExecutorService.shutdown();
        } else if (event instanceof ServletRequestHandledEvent) {
            ServletRequestHandledEvent servletRequestHandledEvent = (ServletRequestHandledEvent) event;
            if (!servletRequestHandledEvent.wasFailure()) {
                SystemLog.LOG(SystemLog.LogType.REQUEST).info(servletRequestHandledEvent.toString());
            } else {
                SystemLog.LOG(SystemLog.LogType.REQUEST).info("error:" + servletRequestHandledEvent.toString());
            }
        } else if (event instanceof EmbeddedServletContainerInitializedEvent) {

        } else {
            System.out.println("else");
            System.out.println(event);
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

    @Override
    public void setEnvironment(Environment environment) {
        SpringUtil.environment = environment;
    }

    public static Environment getEnvironment() {
        Assert.notNull(applicationContext, "environment is null");
        return environment;
    }

    public static String getApplicationId() {
        return getEnvironment().getProperty(CommonPropertiesFinal.APPLICATION_ID);
    }
}

class SystemInitPackageControl {

    @Value("${initPackageName:}")
    private String initPackageName;

    @Value("${initPackageName.methodName:init}")
    private String methodName;

    private static SystemInitPackageControl systemInitPackageControl;

    /**
     * //     * 系统预加载包名
     * //
     */
//    @Value("${server.initPackageName:com.yoke.system.init}")
//    public String initPackageName;
    static void init() {
        if (systemInitPackageControl == null)
            systemInitPackageControl = SpringUtil.getBean(SystemInitPackageControl.class);
        if (StringUtil.isEmpty(systemInitPackageControl.initPackageName))
            return;
        try {
            List<String> list = PackageUtil.getClassName(systemInitPackageControl.initPackageName);
            if (list == null)
                return;
            for (String name : list) {
                try {
                    if (name.contains("$"))
                        continue;
                    Class<?> cls = Class.forName(name);
                    if (cls == SystemInitPackageControl.class)
                        continue;
                    Method method = cls.getMethod(systemInitPackageControl.methodName);
                    method.invoke(null);
                } catch (ClassNotFoundException e) {
                    SystemLog.ERROR().error("预加载包错误:" + name, e);
                } catch (Exception e) {
                    SystemLog.ERROR().error("预加载包错误:" + name + "  执行错误", e);
                }
            }
        } catch (IOException e) {
            SystemLog.ERROR().error("预加载包错误", e);
        }
    }
}

