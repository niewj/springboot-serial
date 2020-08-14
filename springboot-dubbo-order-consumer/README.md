spring开发笔记之-springboot集成dubbo
tag: v1.0 springboot-dubbo-集成-注解版(1)
---
# dubbo开发笔记

## 1. 分布式服务架构以及Dubbo特性一览

### 1.1 服务拆分

>  用户服务	订单服务	商品服务

进程独立



### 1.2 RPC:远程过程调用 

RPC基本原理: 连接-参数传递-序列化-调用返回-反序列化, 核心:

> 1. 建立连接-通讯
> 2. 序列化和反序列化



### 1.3 Dubbo特性一览

> 1. 面向接口代理的高性能RPC调用
> 2. 智能负载均衡
> 3. 服务注册与发现
> 4. 高度可扩展
> 5. 运行期流量调度
> 6. 可视化的服务治理与运维



## 2. Dubbo预备工作: zookeeper

## 2.1 zk配置文件

复制conf下配置文件一份, 重命名为: zoo.cfg, 修改项如下:

1. zoo.cfg: ` dataDir=../data ` 然后:
2.  zkServer.cmd启动zk
3. zkCli.cmd客户端查看和操作

其他详情略



## 3. Dubbo预备工作: dubbo-admin

### 1. 下载 dubbo-admin

> https://github.com/apache/dubbo-admin

### 2. 进入文件夹, mvn clean package

生成jar, 比如这里改名为: dubbo-admin.jar

### 3. 打开命令行 java -jar dubbo-admin.jar

### 4. 7001端口访问

> 用户名密码都是: root



## 4. springboot整合Dubbo-(1)-注解方式

springboot整合: @Service暴露服务 @Reference引用服务

> 已打tag保存: `boot_dubbo_anno`
>
> 

### 4.1 idea建项目-项目模块

```properties
#1 接口服务, 接口都在此模块
springboot-dubbo-interface
#2. 订单服务, 需要调用user的地址服务, 服务消费方
springboot-dubbo-order-consumer
#3. 用户服务, 提供user地址服务, 服务提供方
springboot-dubbo-user-provider
```

### 4.2 接口模块(springboot-dubbo-interface)

1. springboot-dubbo-interface: maven依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.niewj</groupId>
    <artifactId>springboot-dubbo-interface</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot-dubbo-interface</name>
    <description>springboot集成dubbo-接口服务</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
    </dependencies>
</project>

```

2. 一个model类:

```java
package com.niewj.springboot.mall.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户地址
 */
@Data
@AllArgsConstructor
public class UserAddress implements Serializable {
	private String userId;
    private String address; //用户地址
    private String username; //收货人
    private String phone; //电话号码
}
```

3. 两个接口

   3.1 OrderService订单接口

   ```java
   package com.niewj.springboot.mall.service;
   
   import com.niewj.springboot.mall.model.UserAddress;
   
   import java.util.List;
   
   public interface OrderService {
   	
   	/**
   	 * 初始化订单
   	 * @param userId
   	 */
   	List<UserAddress> prepareOrder(String userId);
   
   }
   ```

   3.2 UserService用户接口

   ```java
   package com.niewj.springboot.mall.service;
   
   import com.niewj.springboot.mall.model.UserAddress;
   
   import java.util.List;
   
   /**
    * 用户服务
    */
   public interface UserService {
   	
   	/**
   	 * 按照用户id返回所有的收货地址
   	 * @param userId
   	 * @return
   	 */
   	List<UserAddress> getAddresses(String userId);
   
   }
   ```

### 4.3 订单服务(springboot-dubbo-order-consumer)

1. maven依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.niewj</groupId>
    <artifactId>springboot-dubbo-order-consumer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot-dubbo-order-consumer</name>
    <description>springboot集成dubbo-consumer</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!--        导入接口-->
        <dependency>
            <groupId>com.niewj</groupId>
            <artifactId>springboot-dubbo-interface</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <!--     Dubbo 依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>2.7.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.7.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>4.3.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

2. Controller

```java
package com.niewj.springboot.controller;

import com.niewj.springboot.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by niewj on 2020/8/14 0:41
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/order")
    public Object order(@RequestParam("userId") String userId){
        return orderService.prepareOrder(userId);
    }
}
```

3. 订单实现接口OrderServiceImpl

```java
package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.OrderService;
import com.niewj.springboot.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by niewj on 2020/8/14 10:04
 */
@Slf4j
@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {

    /**
     * dubbo Reference, 指向服务提供方
     * 3. 为远程服务生成代理, 然后就可以像使用本地接口一样使用了
     */
    @Reference
    private UserService userService;

    @Override
    public List<UserAddress> prepareOrder(String userId) {
        System.out.println("用户id：" + userId);
        //1、查询用户的收货地址
        List<UserAddress> addressList = userService.getAddresses(userId);

        if(CollectionUtils.isEmpty(addressList)){
            log.error("addressList is empty");
        }
        List<UserAddress> userAddresses = addressList.stream().filter(addr -> addr.getUserId().equals(userId)).collect(Collectors.toList());
        System.out.println("过滤地址后返回");
        return userAddresses;
    }
}
```

可以看到: `@Reference`这个注解代替了原来的`@Autowired` 这就是dubbo提供的, 用于在consumer端引用provider方提供的服务声明; `@Reference`的参数有:

> ```java
> @Reference(timeout = 2000, check = false, retries = 2, version = "1.0")
> private UserService userService;
> ```
>
> 1. **timeout**:  consumer超时时间: 默认是1000ms;
>    优先级顺序:
>
>           1. 精确优先(方法超时时间)>接口级别次之>全局配置最后
>           2. 如果级别一样, 消费方优先>提供方次之;
>
> 2. **check**:  配置消费者规则
>
>    也可以在application.properties配置文件中统一配置consumer: 不用每个<dubbo:reference> 都单独配置, 统一对所有的起作用!
>
>    ```properties
>    # 可以先启动 consumer->没有provider也可以启动, 不检查注册中心是否有provider
>    dubbo.consumer.check=false
>    ```
>
> 3. **retries**: 重试次数(第一次调用不计次)
>
>    `retries=3`表示允许调用`4`次; 
>
>    而且如果同一个方法有多个提供方在注册中心, 会挨个儿调用;不会一棵树上吊死;
>
>    幂等方法上可以设置重试(查询/删除/修改); 非幂等方法就不要了(新增)!
>
> 4. **version**: 多版本, 如果指定版本version="1.0.0", 就会只调用1.0.0的提供方版本

4. 启动类main方法类:

```java
package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo // 开启dubbo
@SpringBootApplication
public class OrderConsumerApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApp.class, args);
    }
}
```

注意: @EnableDubbo // 开启dubbo

5. application.properties:

```properties
server.port=8082

# 1. 消费端应用名, 用于追踪依赖关系(非标准), 不要将他设置为何provider相同的
dubbo.application.name=order-consumer

# 2. 使用 zookeeper 注册中心发现服务
dubbo.registry.address=zookeeper://127.0.0.1:2181

# dubbo 可以先启动 consumer->没有provider也可以启动, 不检查注册中心是否有provider
dubbo.consumer.check=false
```

### 4.4 用户服务(springboot-dubbo-user-provider)

用户服务, 提供user地址服务, 服务提供方

1. maven:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.niewj</groupId>
    <artifactId>springboot-dubbo-user-provider</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot-dubbo-user-provider</name>
    <description>springboot集成dubbo-用户服务provider</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.niewj</groupId>
            <artifactId>springboot-dubbo-interface</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <!-- Dubbo 依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>2.7.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.7.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>4.3.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

2. provider方: UserServiceImpl实现方法:

```java
package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by niewj on 2020/8/14 9:57
 */

/**
 * 4. 暴露的服务, interface供他人调用, 全路径接口名; ref引用实现类的bean id
 */
@org.apache.dubbo.config.annotation.Service // dubbo Service 声明
@org.springframework.stereotype.Service // spring Service
public class UserServiceImpl implements UserService {
    @Override
    public List<UserAddress> getAddresses(String userId) {

        System.out.println("@@--UserServiceImpl........");
        UserAddress addr1 = new UserAddress("1", "北京亦庄开发区1号", "小王", "010-10102010");
        UserAddress addr2 = new UserAddress("2", "北京亦庄开发区2号", "老王", "010-20102010");
        UserAddress addr3 = new UserAddress("3", "西安曲江创意谷1期", "小张", "029-29296259");
        UserAddress addr4 = new UserAddress("4", "西安曲江创意谷2期", "老张", "029-39296259");
        System.out.println("@@--getAddresses-version-调用");
        return Arrays.asList(addr1, addr2, addr3, addr4);
    }
}
```

3. 入口main方法:

```java
package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo // 开启dubbo
@SpringBootApplication
public class UserProviderApp {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApp.class, args);
    }
}
```

注意: @EnableDubbo // 开启dubbo

4. application.properties

```properties
server.port=8081

# 1. 指定服务名
dubbo.application.name=user-provider

# 2. 指定注册中心位置
dubbo.registry.protocol=zookeeper
dubbo.registry.address=127.0.0.1:2181
# dubbo.registry.address=zookeeper://127.0.0.1:2181

# 3. 指定通信规则(包括协议/端口) 端口自定义, 比如20880
dubbo.protocol.port=20880
dubbo.protocol.name=dubbo
```

### 4.5 服务调用

1. dubbo管理页面: 

   http://localhost:7001/governance/providers可以看到提供方

   http://localhost:7001/governance/consumers可以看到消费方

2. 在浏览器访问: http://localhost:8082/order?userId=2 调用:

   ```json
   [{"userId":"2","address":"北京亦庄开发区2号","username":"老王","phone":"010-20102010"}]
   ```

   OK!

## 5. springboot整合Dubbo-(2)-保留xml配置方式

### 5.1 项目模块同上

### 5.2 接口模块(springboot-dubbo-interface)同上

### 5.3 订单模块(springboot-dubbo-order-consumer)

1. maven依赖 同上4.3
2. Controller 同上4.3
3. 订单实现接口OrderServiceImpl

```java
package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.OrderService;
import com.niewj.springboot.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by niewj on 2020/8/14 10:04
 */
@Slf4j
@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {

    /**
     * dubbo Reference, 指向服务提供方
     * 3. 为远程服务生成代理, 然后就可以像使用本地接口一样使用了
     */
//    @Reference
    @Autowired
    private UserService userService;

    @Override
    public List<UserAddress> prepareOrder(String userId) {
        System.out.println("用户id：" + userId);
        //1、查询用户的收货地址
        List<UserAddress> addressList = userService.getAddresses(userId);

        if (CollectionUtils.isEmpty(addressList)) {
            log.error("addressList is empty");
        }
        List<UserAddress> userAddresses = addressList.stream().filter(addr -> addr.getUserId().equals(userId)).collect(Collectors.toList());
        System.out.println("过滤地址后返回");
        return userAddresses;
    }
}
```

consumer消费, 不使用注解` @Reference  `了, 而是换回 ` @Autowired `

4. 启动类main方法类:

```java
package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

//@EnableDubbo // 开启dubbo
@SpringBootApplication
@ImportResource(locations = "classpath:consumer.xml")
public class OrderConsumerApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApp.class, args);
    }
}
```

去掉 ` @EnableDubbo `;

把 `  consumer.xml `配置文件加入容器管理: ` @ImportResource(locations = "classpath:consumer.xml")`

5. application.properties 只配端口, 原来的注解时的配置都去掉, 仰赖 `consumer.xml`配置文件

```properties
server.port=8082
```

6. resources/consumer.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--suppress ALL -->
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
       http://dubbo.apache.org/schema/dubbo http://dubbo.apache.org/schema/dubbo/dubbo.xsd">

    <!-- 1. 消费端应用名, 用于追踪依赖关系(非标准), 不要将他设置为何provider相同的   -->
    <dubbo:application name="mall-order-consumer"/>

    <!-- 2. 使用 zookeeper 注册中心发现服务  -->
    <dubbo:registry address="zookeeper://127.0.0.1:2181" />

    <!-- 3. 为远程服务生成代理, 然后就可以像使用本地接口一样使用了 -->
    <dubbo:reference id="userService" check="false" interface="com.niewj.springboot.mall.service.UserService" >
        <dubbo:method name="getAddresses" timeout="5000"/>
    </dubbo:reference>

    <!--    连接监控中心:通过注册中心去发现, 非直连-->
<!--    <dubbo:monitor protocol="registry" />-->
</beans>
```

### 5.4 用户服务(springboot-dubbo-user-provider)

1. maven 同上 4.4
2. provider方: UserServiceImpl实现方法:

```java
package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by niewj on 2020/8/14 9:57
 */

/**
 * 4. 暴露的服务, interface供他人调用, 全路径接口名; ref引用实现类的bean id
 */
//@org.apache.dubbo.config.annotation.Service // dubbo Service 声明
@org.springframework.stereotype.Service // spring Service
public class UserServiceImpl implements UserService {
    @Override
    public List<UserAddress> getAddresses(String userId) {

        System.out.println("@@--UserServiceImpl........");
        UserAddress addr1 = new UserAddress("1", "北京亦庄开发区1号", "小王", "010-10102010");
        UserAddress addr2 = new UserAddress("2", "北京亦庄开发区2号", "老王", "010-20102010");
        UserAddress addr3 = new UserAddress("3", "西安曲江创意谷1期", "小张", "029-29296259");
        UserAddress addr4 = new UserAddress("4", "西安曲江创意谷2期", "老张", "029-39296259");
        System.out.println("@@--getAddresses-version-调用");
        return Arrays.asList(addr1, addr2, addr3, addr4);
    }
}
```

如上, 注释掉dubbo的 @Service注解, 改用provider.xml配置中的

> //@org.apache.dubbo.config.annotation.Service // dubbo Service 声明

3. main方法入口:

```java
package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

//@EnableDubbo // 开启dubbo
@SpringBootApplication
@ImportResource(locations = "classpath:provider.xml")
public class UserProviderApp {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApp.class, args);
    }
}
```

关掉 dubbo注解 

>  //@EnableDubbo // 开启dubbo

开启容器导入provider.xml配置管理: 

> @ImportResource(locations = "classpath:provider.xml")

4. application.properties 只留端口配置, 其他使用 provider.xml

```properties
server.port=8081

```

5. resources/provider.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--suppress SpringFacetInspection -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd http://dubbo.apache.org/schema/dubbo http://dubbo.apache.org/schema/dubbo/dubbo.xsd">


    <!-- 1. 指定服务名 -->
    <dubbo:application name="mall-user-provider"  />

    <!-- 2. 指定注册中心位置 -->
    <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181" />

    <!-- 3. 指定通信规则(包括协议/端口) 端口随便写, 比如20880 -->
    <dubbo:protocol name="dubbo" port="20880" />

    <!-- 4. 暴露的服务, interface供他人调用, 全路径接口名; ref引用实现类的bean id -->
    <dubbo:service interface="com.niewj.springboot.mall.service.UserService" ref="userServiceImpl" />

    <!-- 5.   服务的实现-->
<!--    <bean id="userServiceImpl" class="com.niewj.springboot.service.impl.UserServiceImpl" />-->

    <!--    连接监控中心:通过注册中心去发现, 非直连-->
<!--    <dubbo:monitor protocol="registry" />-->

</beans>
```



## 6. dubbo知识点

1. 优先级顺序:

![image-20200814005000833](C:\Users\weiju\AppData\Roaming\Typora\typora-user-images\image-20200814005000833.png)

2. 启动时检查(消费者)

   ```xml
   <dubbo:reference id="userService" check="false" interface="com.niewj.mall.service.UserService"/>
   ```

   check=false; 如果消费者不设置这个, 启动时就会先去查注册中心有没有provider, 没有就报错;

   如果设置了check="false", 就指明了不做检查, 消费者可以先启动, 调用的时候才做判断;

3. 统一配置消费者规则:

   不用每个<dubbo:reference> 都单独配置, 统一对所有的起作用!

   ```xml
   <dubbo:consumer check="false" />
   ```

4. consumer超时时间: 默认是1000ms;

   ```xml
   <dubbo:reference id="userService" check="false" interface="com.niewj.mall.service.UserService" timeout="5000">
      <dubbo:method name="callx" timeout="1000" ></dubbo:method>
   </dubbo:reference>
   
   <dubbo:consumer check="false" timeout="2000" />
   ```

   优先级顺序:

   1. 精确优先(方法超时时间)>接口级别次之>全局配置最后
   2. 如果级别一样, 消费方优先>提供方次之;

5. 重试次数

   retries=3表示允许调用4次; 

   而且如果同一个方法有多个提供方在注册中心, 会挨个儿调用;不会一棵树上吊死;

   幂等方法上可以设置重试(查询/删除/修改); 非幂等方法就不要了(新增)!

   ```xml
   <dubbo:reference id="userService" check="false" interface="com.niewj.mall.service.UserService" retries="3">
   </dubbo:reference>
   ```

6. 多版本

   ```xml
   <dubbo:reference id="userService" check="false" interface="com.niewj.mall.service.UserService" version="1.0.0">
   </dubbo:reference>
   ```
