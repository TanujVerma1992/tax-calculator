package org.grpc.tax.calculator.common;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class ResponseObserver<T> implements StreamObserver<T> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private List<T> result = new ArrayList<>();
    @Override
    public void onNext(T value) {
        log.info("Received : {}",value);
        result.add(value);
    }

    @Override
    public void onError(Throwable t) {
        log.info("Error : {}",t.getMessage());
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        log.info("Completed The Streaming.");
        countDownLatch.countDown();
    }

    public void await(){
        try {
            countDownLatch.await(50, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error : {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
