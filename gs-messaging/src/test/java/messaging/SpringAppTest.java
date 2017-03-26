package messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created by JO088HA on 3/23/2017.
 */
@AutoConfigureMockMvc
@ContextConfiguration(classes = { SpringApp.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringAppTest {

    @LocalServerPort
    private int testPort;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuotePublishController quotePublishController;

    @Autowired
    private TestRestTemplate restTemplate;

    public void testControllerLoads () throws Exception {
        assertThat(quotePublishController).isNotNull();
    }

    @Test
    public void testQuotePublicsherResponse () throws Exception {
        assertThat(this.restTemplate.getForObject(
                "http://localhost:"+ testPort +"/1", String.class)
        ).contains("success");
    }

    @Test
    public void testQuotePublisherMockMVC() throws Exception {
        this.mockMvc
                .perform(get("/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.value").exists());
    }
}
