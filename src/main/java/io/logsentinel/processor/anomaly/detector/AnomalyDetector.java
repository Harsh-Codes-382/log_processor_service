package io.logsentinel.processor.anomaly.detector;

import io.logsentinel.core.model.LogEvent;
import io.logsentinel.processor.anomaly.model.AnomalyResult;

import java.util.Optional;

public interface AnomalyDetector {
    Optional<AnomalyResult> detect(LogEvent log);
}
