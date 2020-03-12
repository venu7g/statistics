package com.solactive.statistics.service;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;

public interface IStatisticsService {
    void addStatistics(StatisticsRequest statisticsRequest);
    StatisticsResponse getStatistics(String key);
}
