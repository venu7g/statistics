package com.solactive.statistics.controller;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;
import com.solactive.statistics.service.IStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatisticsControllerTest {

    @Autowired
    private StatisticsController statisticsController;

    @Autowired
    private IStatisticsService statisticsService;
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Test
    public void whenTimeStampOlderThanCurrentTimeThenReturn204IgnoredInstrumentTicks() throws Exception {
        HttpEntity<StatisticsRequest> request = new HttpEntity<>(statisticsRequest("IBMZERO",121.21,0) , headers());
        ResponseEntity<StatisticsResponse> result = this.restTemplate.postForEntity("http://localhost:" + port + "/ticks", request, StatisticsResponse.class);
        assertEquals(204,result.getStatusCode().value());
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics/IBMZERO"),StatisticsResponse.class);
        assertEquals(0, returnResult.getBody().getCount());
    }
    @Test
    public void testAddInstrumentTicksStatistics() throws Exception {
        HttpEntity<StatisticsRequest> request = new HttpEntity<>(statisticsRequest("IBMSINGLE",121.21,System.currentTimeMillis()) , headers());
        ResponseEntity<StatisticsResponse> result = this.restTemplate.postForEntity("http://localhost:" + port + "/ticks", request, StatisticsResponse.class);
        assertEquals(201,result.getStatusCode().value());
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics/IBMSINGLE"),StatisticsResponse.class);
        assertTrue(returnResult.getBody().getCount()== 1);

    }
    @Test
    public void testAddTicks() throws Exception {
        int noOfRequest = 10;
        addTicks(noOfRequest);
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics"),StatisticsResponse.class);
        assertTrue(returnResult.getBody().getCount()>0);

    }

    private void addTicks(int noOfRequest) {
        IntStream.range(0, noOfRequest).parallel().forEach(
                intStr -> {
                    try {
                        HttpEntity<StatisticsRequest> request = new HttpEntity<>(statisticsRequest("IBM",121.21,System.currentTimeMillis()) , headers());
                        ResponseEntity<StatisticsResponse> result = this.restTemplate.postForEntity("http://localhost:" + port + "/ticks", request, StatisticsResponse.class);
                        assertEquals(201,result.getStatusCode().value());
                    } catch (Exception ex) {}
                }
        );
    }

    @Test
    public void testGetStatistics() throws Exception {
        int noOfRequest = 7;
        addTicks(noOfRequest);
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics"),StatisticsResponse.class);
        assertTrue(returnResult.getBody().getCount()>0);

    }
    @Test
    public void testGetStatisticsByInstrumentId() throws Exception {
        int noOfRequest = 7;
        addTicks(noOfRequest);
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics/IBM"),StatisticsResponse.class);
        assertTrue(returnResult.getBody().getCount()>0);

    }

    @Test
    public void testAddTicksAndRetrieveStatisticsWithConcurrentThreadsWith60SecWindowTime() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(7);
        IntStream.range(0, 2000).forEach(
                intStr -> statisticsController.addTicks(statisticsRequest("IBM",121.21,System.currentTimeMillis()))
        );
        executorService.shutdown();
        Thread.sleep(2000);
        StatisticsResponse statisticsResponse = statisticsController.getStatistics();
        assertTrue(statisticsResponse.getCount()== 2000);
    }
    //@Test//this will take time to execute thread will sleep for 60 seconds
    public void testAddTicksAndRetrieveStatisticsWithConcurrentThreadsGreaterThan60SecWindowTime() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(7);
        IntStream.range(0, 2000).forEach(
                intStr -> statisticsController.addTicks(statisticsRequest("IBM",121.21,System.currentTimeMillis()))
        );
        executorService.shutdown();
        Thread.sleep(60*1000);
        ResponseEntity<StatisticsResponse> returnResult = this.restTemplate.getForEntity(new URI("http://localhost:" + port + "/statistics"),StatisticsResponse.class);
        assertTrue(returnResult.getBody().getCount()== 0);
    }


    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type","application/json");
        return headers;
    }

    private StatisticsRequest statisticsRequest(String instrumentName,Double price,long timeStamp) {
        StatisticsRequest statisticsRequest = new StatisticsRequest();
        statisticsRequest.setTimestamp(timeStamp);
        statisticsRequest.setInstrument(instrumentName);
        statisticsRequest.setPrice(price);
        return statisticsRequest;
    }


}