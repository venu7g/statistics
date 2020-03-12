package com.solactive.statistics.monitoring;

import com.solactive.statistics.model.StatisticsRequest;
import com.solactive.statistics.model.StatisticsResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class SlidingWindowMonitor {
    private static AtomicLong lastSlide;
    private int intervalInSeconds;
    private AtomicReference<ConcurrentHashMap<String, AtomicReference<StatisticsResponse>>> currentBucket;
    private final ConcurrentLinkedQueue<ConcurrentHashMap<String, AtomicReference<StatisticsResponse>>> buckets;
    protected int numberOfBuckets;
    public static final String TotalKey = "total";

    public SlidingWindowMonitor(int numberOfBuckets, int secondsPerBucket) {
        buckets = new ConcurrentLinkedQueue<>();
        currentBucket = new AtomicReference<>();
        intervalInSeconds = secondsPerBucket;
        for (int i = 0; i < numberOfBuckets; i++) {
            ConcurrentHashMap<String, AtomicReference<StatisticsResponse>> newMap =
                    new ConcurrentHashMap<>();
            initBucketMap(newMap);
            currentBucket.set(newMap);
            buckets.add(newMap);
        }
        lastSlide = new AtomicLong(currentTimeMillis());
        this.numberOfBuckets = numberOfBuckets;
    }

    /**
     * Initializes an empty bucket
     *
     * @param bucket
     */
    public void initBucketMap(ConcurrentHashMap<String, AtomicReference<StatisticsResponse>> bucket) {
        AtomicReference<StatisticsResponse> statisticsResponseAtomicReference = new AtomicReference<>();
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponseAtomicReference.set(statisticsResponse);
        bucket.put(TotalKey, statisticsResponseAtomicReference);
    }

    /**
     * Returns the number of buckets to slide
     */
    private int needToSlide() {
        long timeInMillis = this.currentTimeMillis() - lastSlide.get();

        int bucketsToSlide = (int) (timeInMillis / (intervalInSeconds * 1000));

        return (bucketsToSlide < buckets.size()) ? bucketsToSlide : buckets.size();
    }

    /**
     * Checks whether or not window needs to slide
     */
    private void updateWindow() {
        int numOfSlides = needToSlide();

        if (numOfSlides > 0) {
            // Protects multiple threads from wiping out buckets
            synchronized (buckets) {
                numOfSlides = needToSlide();

                // Remove the oldest buckets & add fresh ones
                for (int i = 0; i < numOfSlides; i++) {
                    ConcurrentHashMap<String, AtomicReference<StatisticsResponse>> newMap =
                            new ConcurrentHashMap<>();
                    initBucketMap(newMap);
                    buckets.poll();
                    buckets.add(newMap);
                    currentBucket.set(newMap);
                    lastSlide = new AtomicLong(currentTimeMillis());
                }
            }
        }
    }

    /**
     * Adds the error response to the current bucket
     *
     * @param statisticsRequest
     */
    public void addStatistics(StatisticsRequest statisticsRequest) {
        StatisticsResponse statisticsResponse, valueStatisticsResponse = null;
        updateWindow();
        ConcurrentHashMap<String, AtomicReference<StatisticsResponse>> bucket = currentBucket.get();
        AtomicReference<StatisticsResponse> value2 = new AtomicReference<>();
        statisticsResponse = new StatisticsResponse();
        value2.set(statisticsResponse);
        AtomicReference<StatisticsResponse> value = bucket.computeIfAbsent(statisticsRequest.getInstrument(), key -> value2);
        valueStatisticsResponse = new StatisticsResponse();
        valueStatisticsResponse.accept(statisticsRequest.getPrice());
        if (value == null) {
            value = value2;
        }

        value.getAndUpdate(prev -> {
            prev.accept(statisticsRequest.getPrice());
            return prev;
        });

        AtomicReference<StatisticsResponse> total = bucket.computeIfAbsent(TotalKey, key -> value2);
        if (total == null) {
            total = value2;
        }
        total.getAndUpdate(prev -> {
            prev.accept(statisticsRequest.getPrice());
            return prev;
        });

    }


    /**
     * Returns a copy of the list of buckets
     */
    public List<Map<String, StatisticsResponse>> getBuckets() {
        updateWindow();
        List<Map<String, StatisticsResponse>> ret = new ArrayList<>();

        for (Map<String, AtomicReference<StatisticsResponse>> b : buckets) {
            Map<String, StatisticsResponse> map = new HashMap<>();
            for (Map.Entry<String, AtomicReference<StatisticsResponse>> ent : b.entrySet()) {
                map.put(ent.getKey(), ent.getValue().get());
            }
            ret.add(map);
        }

        return ret;
    }

    /**
     * Returns a copy of the list of buckets
     */
    public StatisticsResponse getStatistics(String key) {
        updateWindow();
        AtomicReference<StatisticsResponse> result = currentBucket.get().get((key == null || key.isEmpty()) ? TotalKey : key);
        return result!=null ? result.get(): new StatisticsResponse();
    }

    /**
     * Returns the date of the window's lastSlide
     */
    public Date getLastSlide() {
        return new Date(lastSlide.get());
    }

    /**
     * Returns the current time in milliseconds, can override for unit testing
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
