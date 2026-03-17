package io.logsentinel.processor.anomaly.model;

import io.logsentinel.processor.anomaly.detector.AnomalyType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class AnomalyResult {
    private ServiceKey serviceKey;
    private Instant timestamp;
    private AnomalyType anomalyType;
    private double score;
    private String reason;
    private Map<String, Object> metadata;
}
