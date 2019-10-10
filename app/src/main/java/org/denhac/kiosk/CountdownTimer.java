package org.denhac.kiosk;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class CountdownTimer {
    private long startTime;
    private Observable<Long> observable;

    public CountdownTimer(long startTime) {
        this.startTime = startTime;

        observable = Observable
                .interval(1, TimeUnit.SECONDS)
                .take(this.startTime + 1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long intervalCount) {
                        return CountdownTimer.this.startTime - intervalCount;
                    }
                });
    }

    public Observable<Long> getObservable() {
        return observable;
    }
}
