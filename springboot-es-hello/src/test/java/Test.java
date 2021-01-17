import com.niewj.SearchApplication;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * Created by niewj on 2021/1/17 23:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class Test {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @org.junit.Test
    public void testRestHighLevelClient(){
        System.out.println(restHighLevelClient);
    }

    @org.junit.Test
    public void testInsert(){

    }
}
