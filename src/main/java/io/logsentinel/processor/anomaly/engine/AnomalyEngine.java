package io.logsentinel.processor.anomaly.engine;

import io.logsentinel.core.model.LogEvent;
import io.logsentinel.processor.anomaly.detector.AnomalyDetector;
import io.logsentinel.processor.anomaly.model.AnomalyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnomalyEngine {
    private final List<AnomalyDetector> detectors;

    public List<AnomalyResult> process(LogEvent log){
        List<AnomalyResult> results = new ArrayList<>();

        for(AnomalyDetector detector : detectors){
            detector.detect(log).ifPresent(results::add);
        }
        return results;
    }

}
