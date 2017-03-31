package messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Test
    public void testControllerLoads() throws Exception {
        assertThat(quotePublishController).isNotNull();
    }

    @Test
    public void testQuotePublisherMockMVC() throws Exception {
        MvcResult mvcPromise = this.mockMvc
            .perform(get("/1"))
            .andDo(print())
            .andReturn();

        /* Testing an asynchronous rest controller with
         * MockMvc requires that the request/response cycle
         * be split into two method calls
         */
        this.mockMvc
            .perform(asyncDispatch(mvcPromise))
            .andExpect(status().isOk())
            //.andExpect(jsonPath("$.result").exists());
            .andExpect(jsonPath("$.*.quote").exists());
    }

    @Test
    public void testQuotePublisherResponse () throws Exception {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        ListenableFuture<ResponseEntity<String>> asyncResponse;

        asyncResponse = asyncRestTemplate.getForEntity("http://localhost:"+ this.testPort +"/", String.class);
        asyncResponse.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

            /* Testing an asynchronous rest controller with
             * AsyncRestTemplate requires that the assetion
             * be placed into the onSuccess callback of a
             * ListenableFuture<ResponseEntity<T>>
             */
            @Override
            public void onSuccess(ResponseEntity<String> stringResponseEntity) {
                assertThat(stringResponseEntity.getBody()).contains("result");
            }

            @Override
            public void onFailure(Throwable throwable) {
                assertThat(asyncResponse.isCancelled());
            }
        });
    }

    @Test
    public void testAsyncQuotePublisherResponses() throws Exception {
        final int targetReqCount = 100;
        final long startTime = System.currentTimeMillis();

        AtomicInteger reqCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(targetReqCount);

        //Stream<String> testRequests = Stream.generate(() -> "/").limit(100);
        Flux<Integer> testRequests = Flux.range(1, targetReqCount);

        /* Asynchronouse requests made in background thread(s) */
        testRequests
            .log()
            .doOnNext(v -> System.err.println(reqCount.incrementAndGet() + ") Get response for http://localhost:" + this.testPort + "/"))
            .flatMap(
                v -> Mono.just(v).subscribeOn(Schedulers.parallel()),
                4
            )
            .subscribe(v -> {

                AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
                ListenableFuture<ResponseEntity<String>> asyncResponse;

                asyncResponse = asyncRestTemplate.getForEntity("http://localhost:"+ this.testPort + "/", String.class);
                asyncResponse.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

                    /* Testing an asynchronous rest controller with
                     * AsyncRestTemplate requires that the assetion
                     * be placed into the onSuccess callback of a
                     * ListenableFuture<ResponseEntity<T>>
                     */
                    @Override
                    public void onSuccess(ResponseEntity<String> stringResponseEntity) {
                        latch.countDown();

                        assertThat(stringResponseEntity.getBody()).contains("result");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        latch.countDown();

                        assertThat(asyncResponse.isCancelled());
                    }
                });

            });

        final long stopTime = System.currentTimeMillis() - startTime;

        /* Synchronous await timeout in main thread to keep test app alive */
        latch.await();

        System.err.println(reqCount.get() + " requests made in " + (int) (stopTime) + "ms");
    }
}
