package com.niewj.springboot.config;

import com.niewj.springboot.mall.service.UserService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Created by niewj on 2020/8/14 14:33
 */
@Configuration
public class DubboConsumerConfig {

    /**
     *     <!-- 1. 消费端应用名, 用于追踪依赖关系(非标准), 不要将他设置为何provider相同的   -->
     *     <dubbo:application name="mall-order-consumer"/>
     * @return
     */
    @Bean
    public ApplicationConfig applicationConfig(){
        ApplicationConfig application = new ApplicationConfig();
        application.setName("mall-order-consumer");
        return application;
    }

    /**
     *     <!-- 2. 使用 zookeeper 注册中心发现服务  -->
     *     <dubbo:registry address="zookeeper://127.0.0.1:2181" />
     */
    @Bean
    public RegistryConfig registryConfig(){
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");
        return registry;
    }

    /**
     * <!-- 3. 为远程服务生成代理, 然后就可以像使用本地接口一样使用了 -->
     *     <dubbo:reference id="userService" check="false" interface="com.niewj.springboot.mall.service.UserService" >
     *         <dubbo:method name="getAddresses" timeout="5000"/>
     *     </dubbo:reference>
     */
    @Bean
    public ReferenceConfig<UserService> referenceConfig(){
        MethodConfig method = new MethodConfig();
        method.setName("getAddresses");
        method.setTimeout(1000);

        ReferenceConfig<UserService> reference = new ReferenceConfig();
        reference.setInterface(UserService.class);
        reference.setId("userService");
        reference.setMethods(Arrays.asList(method));

        return reference;
    }

}
