package com.niewj.springboot;

import com.google.gson.Gson;
import com.niewj.springboot.model.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootTest {

    @Autowired
    private Person person;

    @Autowired
    private ApplicationContext ctx;


    @Test
    public void testImportResurce() {
        Optional<String> restTemplate = Arrays.stream(ctx.getBeanDefinitionNames()).filter(s -> s.equalsIgnoreCase("restTemplate")).findAny();
        String template = restTemplate.orElse(null);
        if(null != template){
            System.out.println(ctx.getBean(RestTemplate.class));
        }
    }

    @Test
    public void printPerson() {
        System.out.println(new Gson().toJson(person));
    }

}
