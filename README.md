# Project Title

POC application of Circuit Breaking Technology

#Getting Started

##Prerequisites

Install Intellij IDE and Java 8 on your local machine.

##Installing

Open service app called `poc` and client app called `client` in intellij

Build and run them there.

##Running the tests

Input two urls as follow in your browser.

```
localhost:8090/recommended

localhost:8080
```
##Break down into end to end tests

Use 2th url to test circuit breaking technology. If default state or clicking `HOME` button here, 
we can see that circuit breaking app is working correctly.
If clicking `OPEN` button, we can see `close` state of circuit breaking app, because of controlling error state manually.
Besides, we can see another value of circuit breaking app.

##And coding

```$xslt
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .ringBufferSizeInHalfOpenState(2)
                .ringBufferSizeInClosedState(2)
                .build();
```

We can change the values of CircuitBreakerConfig's parameters.

Please refer to [Resilience4j](https://www.baeldung.com/resilience4j).
# poc
