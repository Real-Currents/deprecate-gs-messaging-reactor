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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by JO088HA on 3/23/2017.
 */
@AutoConfigureMockMvc
@ContextConfiguration(classes = {SpringApp.class})
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

    public void testControllerLoads() throws Exception {
        assertThat(quotePublishController).isNotNull();
    }

//    @Test
//    public void testQuotePublisherMockMVC() throws Exception {
//        this.mockMvc
//            .perform(get("/1"))
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.result.value").exists());
//    }

//    @Test
//    public void testQuotePublisherResponse () throws Exception {
//        assertThat(this.restTemplate.getForObject(
//                "http://localhost:"+ testPort +"/", String.class)
//        ).contains("success");
//    }

    @Test
    public void testAsyncQuotePublisherResponses() throws Exception {
        AtomicInteger reqCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(100);
        long startTime = System.currentTimeMillis();

        //Stream<String> testRequests = Stream.generate(() -> "/").limit(100);
        Flux<String> testRequests = Flux.generate(v -> v.next("/"));
//        just(
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/",
//            "/", "/", "/", "/", "/", "/", "/", "/", "/", "/"
//        );

        /* Asynchronouse requests made in background thread(s) */
        testRequests
            .log()
            .flatMap(
                value -> {
                    return Mono.just(value).subscribeOn(Schedulers.parallel());
                },
                4
            )
            //.doOnNext()
//            .filter(v -> {
//                System.err.println(reqCount.incrementAndGet() + ") Get response for http://localhost:" + testPort + v);
//                if (Integer.getInteger(v) < 101) return true; else return false;
//            })
            .subscribe(v -> {
                //System.err.println(reqCount.incrementAndGet() + ") Get response for http://localhost:" + testPort + v);

                assertThat(this.restTemplate.getForObject(
                    "http://localhost:" + testPort + v, String.class)
                ).contains("success");

                latch.countDown();
            }, 100);

        long stopAsyncTime = System.currentTimeMillis() - startTime;

        /* Synchronous timeout made in main thread to keep test app alive */
        latch.await();

        System.err.println(reqCount.get() + " requests completed after " + (int) (stopAsyncTime) + "ms");
    }
}
