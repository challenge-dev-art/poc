package com.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Controller
public class CircuitBreakingController {

    // inject via application.properties
    @Value("${index.message:test}")
    private String message = "Hello World";

    @RequestMapping("/")
    public String readingList(Map<String, Object> model) {

        CheckedFunction0<String> supplier = () -> {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = URI.create("http://localhost:8090/recommended");
            return restTemplate.getForObject(uri, String.class);
        };

        CheckedFunction1<String, String> funcEmpToString= (String argInt)-> {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = URI.create("http://localhost:8090/recommended");

            return restTemplate.getForObject(uri, String.class);
        };

        // Given
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .ringBufferSizeInHalfOpenState(2)
                .ringBufferSizeInClosedState(2)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("open", circuitBreakerConfig);
        CircuitBreaker anotherCircuitBreaker = CircuitBreaker.ofDefaults("anotherTestName");

        // When I create a Supplier and a Function which are decorated by different CircuitBreakers
        CheckedFunction0<String> decoratedSupplier = CircuitBreaker
                .decorateCheckedSupplier(circuitBreaker, supplier);

        CheckedFunction1<String, String> decoratedFunction = CircuitBreaker
                .decorateCheckedFunction(anotherCircuitBreaker, funcEmpToString);

        Try<String> result = null;

        for (int i = 0; i < 10; i++)
        {
            // and I chain a function with map
            result = Try.of(decoratedSupplier).recover(throwable -> "Hello Recovery");//mapTry(decoratedFunction::apply);
        }

        // Then
        assertThat(result.isSuccess()).isTrue();
//		assertThat(result.get()).contains("OK");

        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        // Returns the failure rate in percentage.
        float failureRate = metrics.getFailureRate();
        // Returns the current number of buffered calls.
        int bufferedCalls = metrics.getNumberOfBufferedCalls();
        // Returns the current number of failed calls.
        int failedCalls = metrics.getNumberOfFailedCalls();

        model.put("message", new Result(failureRate, bufferedCalls, failedCalls, result.get(), circuitBreaker.getState()));
        return "index";
    }

    @RequestMapping("/open")
    public String open(Map<String, Object> model ){
        // Given
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .ringBufferSizeInClosedState(2)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("open", circuitBreakerConfig);

        // Simulate a failure attempt
        circuitBreaker.onError(0, new RuntimeException());
        // CircuitBreaker is still CLOSED, because 1 failure is allowed
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        // Simulate a failure attempt
        circuitBreaker.onError(0, new RuntimeException());
        // CircuitBreaker is OPEN, because the failure rate is above 50%
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When I decorate my function and invoke the decorated function
        CheckedFunction0<String> checkedSupplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> {
            throw new RuntimeException("BAM!");
        });
        Try<String> result = Try.of(checkedSupplier)
                .recover(throwable -> "Hello Recovery");

        // Then the call fails, because CircuitBreaker is OPEN
        //	assertThat(result.isFailure()).isTrue();
        // Exception is CircuitBreakerOpenException
        //	assertThat(result.failed().get()).isInstanceOf(CircuitBreakerOpenException.class);

        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        // Returns the failure rate in percentage.
        float failureRate = metrics.getFailureRate();
        // Returns the current number of buffered calls.
        int bufferedCalls = metrics.getNumberOfBufferedCalls();
        // Returns the current number of failed calls.
        int failedCalls = metrics.getNumberOfFailedCalls();

        model.put("message", new Result(failureRate, bufferedCalls, failedCalls, result.get(), circuitBreaker.getState()));
        return "index";
    }
}
