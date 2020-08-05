## 1. springboot简介与helloworld

### 1.1 优点: 

简化spring开发/约定优于配置 :

- 嵌入式的servlet容器;

- starter自动依赖和版本控制;

- 大量自动配置, 简化开发;

- 无需配置xml, 无代码生成, 开箱即用;

- 应用监控:准生产环境的运行时应用监控;

### 1.2 缺点: 

入门容易, 精通难~

### 1.3 微服务

`springboot:`

一个应用, 是一组小型服务, 可以通过http方式进行互通; 每一个功能元素都是一个可以独立替换和独立升级的单元;

`springcloud:`

 整个应用的大型分布式网格之间的调用;

### 1.4 helloworld

#### 1.4.1 maven依赖:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.niewj</groupId>
    <artifactId>springboot-study</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <spring-boot-version>2.3.2.RELEASE</spring-boot-version>
    </properties>
    <modules>
        <module>springboot-01-helloworld</module>
    </modules>

    <!-- 表示是一个pom总的父工程 -->
    <packaging>pom</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>${spring-boot-version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot-version}</version>
        </dependency>

    </dependencies>

</project>
```

#### 1.4.2 类文件:

`SpringBootApplication` 启动类

```java
package com.niewj.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by niewj on 2020/8/4 18:04
 */
@SpringBootApplication
public class HelloApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }
}

```

HelloController.java

```java
package com.niewj.springboot.controller;

import com.niewj.springboot.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by niewj on 2020/8/4 18:08
 */
@Controller
public class HelloController {

    @RequestMapping("/hello")
    @ResponseBody
    public Object hello(){
        return new User("niewj", 33);
    }
}
```

User实体类:

```java
package com.niewj.springboot.model;

/**
 * Created by niewj on 2020/8/4 18:09
 */
public class User {

    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

完结; 访问: http://localhost:8080/hello

返回:

```shell
{"name":"niewj","age":33}
```



## 2. springboot原理初探-解析helloworld

### 2.1 父依赖: starter-parent

`helloworld`查看依赖: 项目依赖了 `spring-boot-stater-parent`:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
</dependency>
```

`spring-boot-stater-parent`点进去, 里面还依赖了个`parent`:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-dependencies</artifactId>
  <version>2.3.2.RELEASE</version>
</parent>
```

`spring-boot-dependencies`再点进去:

一大堆 `properties`

一大堆 `dependencies`

可见:

> *spring-boot-dependencies* 才是springboot 所有依赖真正管理者;



### 2.2 导入的依赖: starter-web

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId> 
</dependency>
```

点进去: spring-boot-starter-web帮我们导入了web项目所依赖的依赖项:  

```
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>2.3.2.RELEASE</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-json</artifactId>
    <version>2.3.2.RELEASE</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <version>2.3.2.RELEASE</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.2.8.RELEASE</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.2.8.RELEASE</version>
    <scope>compile</scope>
  </dependency>
</dependencies>
```

1. `spring-boot-starter` 
2.  `spring-boot-starter-json`
3.  `spring-boot-starter-tomcat` 
4. `spring-web` 
5.  `spring-webmvc`

starter: spring boot帮助管理依赖资源;

spring提供了一系列starter: mail/web/jpa/mq...



### 2.3  @SpringBootApplication注解

注解类: 

```java
//....SpringBootApplication...
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration // 1
@EnableAutoConfiguration // 2
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
      @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

.....
    
//....SpringBootConfiguration...
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration	// 3
public @interface SpringBootConfiguration {
    
.....
    
//....Configuration...
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component // 4
public @interface Configuration {
```

主要注解类: 

### 2.4 SpringBootApplication之SpringBootConfiguration

`SpringBootConfiguration`可以看到, 有 `Configuration`所以:

#### 2.4.1 声明Application类为一个配置类

SpringBootApplication -> SpringBootConfiguration -> Configuration -> Component

> 注解了 *@SpringBootApplication* 就有了 *@SpringBootConfiguration*;
>
> 就有了 *@Configuration* 的功能; 即:此类是配置类!
>
> 就有了 *@Component* 的功能;

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration // 继承了Component
public @interface SpringBootConfiguration {
```

上面说明: HelloApplication.java注解了@SpringBootApplication 它就是一个配置类, 可以被 `AnnotationConfigApplicationContext`加载扫描起来!

### 2.5 SpringBootApplication之EnableAutoConfiguration

> 按需自动注入spring.factories组件

#### 2.5.1 EnableAutoConfiguration注解原理

> 1. 路径: org.springframework.boot.autoconfigure.EnableAutoConfiguration
>
> 2. 此类所属jar包 `spring-boot-autoconfigure.jar` 目录下有 `META-INF/spring.factories`文件, 内容如下: 可以这么理解: 下面所有类都是配置类, 作用都类似于写一个 `@Configuration`的类, 在其中初始化相应所需要的的 `@Bean`;
>
>    ```proper
>    # Initializers
>    org.springframework.context.ApplicationContextInitializer=\
>    org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
>    org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
>    
>    # Application Listeners
>    org.springframework.context.ApplicationListener=\
>    org.springframework.boot.autoconfigure.BackgroundPreinitializer
>    
>    # Auto Configuration Import Listeners
>    org.springframework.boot.autoconfigure.AutoConfigurationImportListener=\
>    org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener
>    
>    # Auto Configuration Import Filters
>    org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
>    org.springframework.boot.autoconfigure.condition.OnBeanCondition,\
>    org.springframework.boot.autoconfigure.condition.OnClassCondition,\
>    org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition
>    
>    # Auto Configure
>    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
>    org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
>    org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
>    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
>    ............省略100多个....
>    
>    # Failure analyzers
>    org.springframework.boot.diagnostics.FailureAnalyzer=\
>    org.springframework.boot.autoconfigure.data.redis.RedisUrlSyntaxFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.diagnostics.analyzer.NoSuchBeanDefinitionFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.flyway.FlywayMigrationScriptMissingFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.jdbc.DataSourceBeanCreationFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.jdbc.HikariDriverConfigurationFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBeanCreationFailureAnalyzer,\
>    org.springframework.boot.autoconfigure.session.NonUniqueSessionRepositoryFailureAnalyzer
>    
>    # Template availability providers
>    org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider=\
>    org.springframework.boot.autoconfigure.freemarker.FreeMarkerTemplateAvailabilityProvider,\
>    org.springframework.boot.autoconfigure.mustache.MustacheTemplateAvailabilityProvider,\
>    org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAvailabilityProvider,\
>    org.springframework.boot.autoconfigure.thymeleaf.ThymeleafTemplateAvailabilityProvider,\
>    org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider
>    
>    ```

#### 2.5.2 EnableAutoConfiguration的继承关系

```java
//....EnableAutoConfiguration...
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage // 5
@Import(AutoConfigurationImportSelector.class) // 6
public @interface EnableAutoConfiguration {}

//....AutoConfigurationPackage...
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationPackages.Registrar.class)	 // 7
public @interface AutoConfigurationPackage {}


// ...Registrar...
// ...ImportBeanDefinitionRegistrar...
/**
 * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
 * configuration.
 */
static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
	}

	@Override
	public Set<Object> determineImports(AnnotationMetadata metadata) {
		return Collections.singleton(new PackageImports(metadata));
	}

}
// ImportBeanDefinitionRegistrar 可以在spring容器bean初始化的时候注入组件到容器中
```

 // 6 处是一个 `ImportSelector`

// 7 处是一个`ImportBeanDefinitionRegistrar`

这两处都是往容器中注册bean, 具体参见笔记:

[spring注解驱动开发-(5) 向Spring容器中注册组件的方法](https://segmentfault.com/a/1190000023213951)

> _ImportBeanDefinitionRegistrar_ 可以在spring容器bean初始化的时候注入组件到容器中: 在这里打断点可以看到: 会把 com.niewj.springboot 包目录注册进去; 这样, 此包下所有子包中的spring注解的类就都会被扫描, 并初始化注册到spring容器的上下文;

![image-20200804202341031](D:\data\notes\typoras\images\SpringBootApplication-EnableAutoConfiguration.png)

这样的话, spring容器在启动容器的时候, 就会按需加载  spring-boot-autoconfigure.jar包下的`META-INF/spring.factories`中配置的加载类; 达到自动配置注入bean到容器中的效果(目的就是方便开发, 不需要可怜的程序员一个一个写@Configuration的类, 然后自己写@Bean的方法注册组件) 这种机制就是 `SPI` [深入理解SPI机制](https://www.jianshu.com/p/3a3edbcd8f24)



## 3. 读配置文件:@ConfigurationProperties

### 3.1 ConfigurationProperties使用方式

#### 3.1.1. 步骤

`@Component+@ConfigurationProperties(prefix="person")`

person是在yml中配置的前缀: person: ...

#### 3.1.2. 样例
2.1 maven依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/>
    </parent>
    <groupId>com.niewj</groupId>
    <artifactId>springboot-01-helloworld</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot-01-helloworld</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient -->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.6</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- @RunWith都是来自它 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.6</version>
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

2.2 样例之:person.java

```java
package com.niewj.springboot.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by niewj on 2020/8/5 0:01
 */
@Data
@Component
@ConfigurationProperties(prefix = "person")
public class Person {
    private String lastName;
    private Integer age;
    private Boolean male;
    private Date birth;

    private Map<String, Object> maps;
    private List<Object> lists;
    private Dog dog;
}
```

2.3 Dog.java

```java
package com.niewj.springboot.model;

import lombok.Data;

/**
 * Created by niewj on 2020/8/5 0:04
 */
@Data
public class Dog {
    private String name;
    private Integer age;
}
```

2.4 application.yml

```yaml
server:
  port: 8888

person:
  lastName: 张三
  age: 33
  male: true
  birth: 1985/03/03
  maps: {k1: v1, k2: 20, k3: true}
  lists:
    - lisi
    - wangwu
  dog:
    name: 小黑
    age: 3

```

2.5 测试用例:

```java
package com.niewj.springboot;

import com.google.gson.Gson;
import com.niewj.springboot.model.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by niewj on 2020/8/5 0:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootTest {

    @Autowired
    private Person person;

    @Test
    public void printPerson(){
        System.out.println(new Gson().toJson(person));
    }
}
```

2.6: 输出:

```json
{
	"lastName": "张三",
	"age": 33,
	"male": true,
	"birth": "Mar 3, 1985 12:00:00 AM",
	"maps": {
		"k1": "v1",
		"k2": 20,
		"k3": true
	},
	"lists": ["lisi", "wangwu"],
	"dog": {
		"name": "小黑",
		"age": 3
	}
}
```

### 3.2 @ConfigurationProperties 和 @Value比较

#### 3.2.1 @ConfiguratonProperties

1. 适合场景: 批量注入配置文件中的属性;

2. 支持 Relax-Binding(送伞绑定);

   如 `Person.firstName` 匹配:

   > person.first-name
   >
   > person.first_name
   >
   > person.firstName
   >
   > PERSON_FIRST_NAME

3. 不支持spEL表达式;

4. JSR303 数据校验支持;

5. 支持复杂类型: 可以取到 maps 的值;

#### 3.2.2 @Value

1. 适合场景: 一个一个指定配置属性;

2. 不支持 Relax-Binding (松散绑定);

3. 支持spEL;

   如 @Value("#{20*2}") // 会计算出 40赋值给字段属性

4. 不支持JSR303数据校验;

5. @Value取不到 maps 的值(不支持复杂类型)
