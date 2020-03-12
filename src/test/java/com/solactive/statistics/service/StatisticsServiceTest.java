package com.solactive.statistics.service;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StatisticsServiceTest {
    @Autowired
    IStatisticsService statisticsService;
    @Test
    public void testAddStatistics(){
        addStatistics(27,"SAP");
        StatisticsResponse statisticsResponse = statisticsService.getStatistics("SAP");
        assertEquals(27,statisticsResponse.getCount());
    }
    @Test
    public void testGetStatistics(){
        addStatistics(107,"IBM");
        StatisticsResponse statisticsResponse = statisticsService.getStatistics("IBM");
        assertEquals(107,statisticsResponse.getCount());
    }

    private void addStatistics(int noOfRequest,String instrumentId) {
        IntStream.range(0, noOfRequest).forEach(
                intStr -> {
                    try {
                       StatisticsRequest statisticsRequest = statisticsRequest(instrumentId,121.21,System.currentTimeMillis());
                       statisticsService.addStatistics(statisticsRequest);
                    } catch (Exception ex) {}
                }
        );
    }
    private StatisticsRequest statisticsRequest(String instrumentName,Double price,long timeStamp) {
        StatisticsRequest statisticsRequest = new StatisticsRequest();
        statisticsRequest.setTimestamp(timeStamp);
        statisticsRequest.setInstrument(instrumentName);
        statisticsRequest.setPrice(price);
        return statisticsRequest;
    }
}
