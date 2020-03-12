package com.solactive.statistics.service.impl;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;
import com.solactive.statistics.monitoring.SlidingWindowMonitor;
import com.solactive.statistics.service.IStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements IStatisticsService {
    private Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private SlidingWindowMonitor slidingWindowMonitor;

    @Autowired
    public StatisticsServiceImpl(@Value("${window.size.ms}") final Integer windowSizeInMs, @Value("${noOf.buckets}") final Integer noOfBuckets) {
        log.debug("Sliding Window size in MS" + windowSizeInMs);
        log.debug("Number Of Buckets" + noOfBuckets);
        slidingWindowMonitor = new SlidingWindowMonitor(noOfBuckets, windowSizeInMs);
    }

    @Override
    @Async
    public void addStatistics(StatisticsRequest statisticsRequest) {
        log.debug("Instrument Name" + statisticsRequest.getInstrument());
        log.debug("Price of the Unit" + statisticsRequest.getPrice());
        slidingWindowMonitor.addStatistics(statisticsRequest);
    }

    @Override
    public StatisticsResponse getStatistics(String key) {
        log.debug("Instrument Name" + key);
        return slidingWindowMonitor.getStatistics(key);
    }

}
