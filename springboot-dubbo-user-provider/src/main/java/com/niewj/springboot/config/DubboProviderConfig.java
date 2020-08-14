package com.niewj.springboot.config;

import com.niewj.springboot.mall.service.UserService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by niewj on 2020/8/14 14:33
 */
@Configuration
public class DubboProviderConfig {

    /**
     * <!-- 1. 指定服务名 -->
     * <dubbo:application name="mall-user-provider"  />
     *
     * @return
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("mall-user-provider");
        return application;
    }

    /**
     * <!-- 2. 指定注册中心位置 -->
     * <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181" />
     */
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registry = new RegistryConfig();
        registry.setProtocol("zookeeper");
        registry.setAddress("127.0.0.1:2181");
        return registry;
    }

    /**
     * <!-- 3. 指定通信规则(包括协议/端口) 端口随便写, 比如20880 -->
     * <dubbo:protocol name="dubbo" port="20880" />
     */
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(20880);

        return protocol;
    }

    /**
     * <!-- 4. 暴露的服务, interface供他人调用, 全路径接口名; ref引用实现类的bean id -->
     * <dubbo:service interface="com.niewj.springboot.mall.service.UserService" ref="userServiceImpl" />
     */
    @Bean
    public ServiceConfig<UserService> userServiceConfig(UserService userService) {
        ServiceConfig<UserService> serviceConfig = new ServiceConfig<>();
        serviceConfig.setRef(userService);
        serviceConfig.setInterface(UserService.class);
        return serviceConfig;
    }

}
