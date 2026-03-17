package io.logsentinel.processor.anomaly.state;

import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.Queue;

@RequiredArgsConstructor
public class RollingStates {
    private final int windowSize;
    private final Queue<Integer> values = new LinkedList<>();
    private double sumSquares = 0.0;
    private double sum = 0;

    public synchronized void add(int value){
        values.add(value);
        sum += value;
        sumSquares += (double) value*value;

        if(values.size() > windowSize){
            int removed = values.poll();
            sum -= removed;
            sumSquares -= (double) removed*removed;
        }
    }

    public synchronized double mean(){
        return values.isEmpty() ? 0 : sum/values.size();
    }

    public synchronized double stdDev(){
        if(values.size() < 2) return 0;

        double mean = mean();
        return Math.sqrt((sumSquares / values.size()) - (mean*mean));
    }

}
