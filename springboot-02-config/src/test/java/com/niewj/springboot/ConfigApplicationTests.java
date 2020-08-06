package com.niewj.springboot;

import com.google.gson.Gson;
import com.niewj.springboot.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ConfigApplicationTests {

    @Autowired
    private Person person;

    @Test
    public void testConfig() {
        System.out.println(new Gson().toJson(person));
    }

}
