package io.logsentinel.processor.anomaly.detector;

import io.logsentinel.core.model.LogEvent;
import io.logsentinel.core.model.LogLevel;
import io.logsentinel.processor.anomaly.model.AnomalyResult;
import io.logsentinel.processor.anomaly.model.ServiceKey;
import io.logsentinel.processor.anomaly.state.RollingStates;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ErrorRateDetector implements AnomalyDetector {
    private static final int WINDOW_SIZE = 10;
    private static final int WINDOW_SECONDS = 60;
    private static final double Z_THRESHOLD = 3.0;

    private final Map<ServiceKey, RollingStates> statsMap = new ConcurrentHashMap<>();
    private final Map<ServiceKey, Integer> windowCount = new ConcurrentHashMap<>();
    private final Map<ServiceKey, Instant> windowStart = new ConcurrentHashMap<>();

    @Override
    public Optional<AnomalyResult> detect(LogEvent log) {
        if (log.getContext() == null) return Optional.empty();

        ServiceKey key = new ServiceKey(
                log.getContext().getServiceName(),
                log.getContext().getEnvironment()
        );

        statsMap.putIfAbsent(key, new RollingStates(WINDOW_SIZE));
        windowCount.putIfAbsent(key, 0);
        windowStart.putIfAbsent(key, Instant.now());

        if(log.getLevel() == LogLevel.ERROR){
            windowCount.computeIfPresent(key, (k, v) -> v + 1);
        }

        Instant start = windowStart.get(key);

        if(Duration.between(start, Instant.now()).getSeconds() >= WINDOW_SECONDS){
            int currentCount = windowCount.get(key);
            RollingStates stats = statsMap.get(key);

            stats.add(currentCount);

            double mean = stats.mean();
            double stdDev = stats.stdDev();
            double Zscore = stdDev == 0 ? 0 : (currentCount - mean) / stdDev;

            if(Zscore >= Z_THRESHOLD){
                AnomalyResult result = new AnomalyResult();
                result.setServiceKey(key);
                result.setTimestamp(Instant.now());
                result.setAnomalyType(AnomalyType.ERROR_RATE_SPIKE);
                result.setScore(Zscore);
                result.setReason("Error rate exceeded 3σ threshold");
                result.setMetadata(Map.of(
                        "currentCount", currentCount,
                        "mean", mean,
                        "stdDev", stdDev
                ));

                return Optional.of(result);
            }
        }
        return Optional.empty();
    }
}
