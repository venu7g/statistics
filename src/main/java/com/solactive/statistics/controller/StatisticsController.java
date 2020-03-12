
package com.solactive.statistics.controller;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;
import com.solactive.statistics.service.IStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController
public class StatisticsController {
    private Logger log = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    IStatisticsService statisticsService;
    @Value("${window.size.ms}")
    private long windowSizeInMs;

    @GetMapping("/statistics")
    public @ResponseBody
    StatisticsResponse getStatistics() {
        return statisticsService.getStatistics(null);
    }
    @GetMapping("/statistics/{instrumentId}")
    public @ResponseBody
    StatisticsResponse getStatisticsByInstrumentId(@PathVariable("instrumentId") @NotEmpty String instrumentId) {
        return statisticsService.getStatistics(instrumentId);
    }

    @PostMapping("/ticks")
    public ResponseEntity addTicks(@Valid @RequestBody StatisticsRequest statisticsRequest) {
        long now = System.currentTimeMillis();
        if (statisticsRequest.getTimestamp() + (windowSizeInMs * 1000 ) < now) {
            log.info(" tick {} is older than {}", statisticsRequest, windowSizeInMs * 1000);
            return ResponseEntity.status(204).build();
        } else {
            log.info("Accepted Instrument ticks {}", statisticsRequest);
            statisticsService.addStatistics(statisticsRequest);
            return ResponseEntity.status(201).build();
        }
    }
}

