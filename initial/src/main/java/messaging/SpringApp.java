package messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.Environment;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static reactor.bus.selector.Selectors.$;

/**
 * Created by revlin on 2/25/17.
 */
@SpringBootApplication
@EnableAsync
public class SpringApp extends AsyncConfigurerSupport { //implements CommandLineRunner {

    private static final int DEFAULT_NUMBER_OF_QUOTES = 10;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private QuoteReceiver receiver;

    @Bean
    public CountDownLatch createLatch() {
        return new CountDownLatch(DEFAULT_NUMBER_OF_QUOTES);
    }

    @Bean
    public Environment getEnvironment() {
        return Environment.initializeIfEmpty()
            .assignErrorJournal();
    }

    @Bean
    public EventBus createEventBus(Environment env) {
        return EventBus.create(env, Environment.THREAD_POOL);
    }

    @Override
    public Executor getAsyncExecutor () {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        eventBus.on($("quotes"), receiver);

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("GithubLookup-");
        executor.initialize();
        return executor;
    }

    public static void main(String[] args) throws InterruptedException {

        ApplicationContext app = SpringApplication.run(SpringApp.class, args);

        app.getBean(CountDownLatch.class).await(1, TimeUnit.SECONDS);

        app.getBean(Environment.class).shutdown();
    }

}
