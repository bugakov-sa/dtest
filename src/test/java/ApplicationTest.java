import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testNotValid() {
        assertEquals("not found", restTemplate.getForObject("/validate?text=qefqfe", String.class));
    }

    @Test
    public void testValid() {
        assertEquals("ok", restTemplate.getForObject("/validate?text=75632574 Ne oplachivat", String.class));
    }
}
