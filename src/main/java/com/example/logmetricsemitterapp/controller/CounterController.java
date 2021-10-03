package com.example.logmetricsemitterapp.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

@RestController
public class CounterController {

    Counter testCounter;

    @Autowired
    MeterRegistry meterRegistry;

    int count = 0;

    public static final Logger LOGGER = LoggerFactory.getLogger(CounterController.class);

    @GetMapping("/v1/counter")
    public int increaseCounter(){
        count ++;
        testCounter = meterRegistry.counter("custom_counter");
        testCounter.increment();
        LOGGER.info(" increase count to " + " " + count+ " " + " " + new Date());
        return count;
    }


    @GetMapping("/v1/exception")
    public String raiseException(){
        String response = "";
        try{
            throw new Exception("Exception has occured");

        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            LOGGER.error("Exception - " + stackTrace);
            response = stackTrace;
        }

        return response;

    }
}
