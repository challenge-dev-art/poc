package com.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public class Result {
    public float failureRate;
    public int bufferedCalls;
    public int failedCalls;

    public String returnValue;
    public CircuitBreaker.State state;

    public Result(float failureRate, int bufferedCalls, int failedCalls, String returnValue) {
        setFailureRate(failureRate);
        setBufferedCalls(bufferedCalls);
        setFailedCalls(failedCalls);
        setReturnValue(returnValue);
    }

    public Result(){}

    public Result(float failureRate, int bufferedCalls, int failedCalls, String s, CircuitBreaker.State state) {
        setFailureRate(failureRate);
        setBufferedCalls(bufferedCalls);
        setFailedCalls(failedCalls);
        setReturnValue(s);
        setState(state);
    }

    public float getFailureRate(){
        return failureRate;
    }
    public void setFailureRate(float failureRate){
        this.failureRate = failureRate;
    }

    public int getBufferedCalls(){
        return bufferedCalls;
    }
    public void setBufferedCalls(int bufferedCalls){
        this.bufferedCalls = bufferedCalls;
    }

    public int getFailedCalls(){
        return failedCalls;
    }
    public void setFailedCalls(int failedCalls){
        this.failedCalls = failedCalls;
    }

    public String getReturnValue(){return returnValue;}
    public void setReturnValue(String returnValue){
        this.returnValue = returnValue;
    }

    public CircuitBreaker.State getState(){
        return state;
    }
    public void setState(CircuitBreaker.State state){
        this.state = state;
    }
}
