package org.grpc.tax.calculator.common;

import com.grpc.tax.calculator.models.TaxCalculatorServiceGrpc;
import com.grpc.tax.calculator.models.TaxCalculatorServiceGrpc.TaxCalculatorServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractChannelTest extends AbstractTest{

    private ManagedChannel channel;
    protected TaxCalculatorServiceStub taxCalculatorServiceStub;

    @BeforeAll
    public void initiateChannel(){
        initiateServer();
        channel = ManagedChannelBuilder.forAddress("localhost",6565).usePlaintext().build();
        taxCalculatorServiceStub = TaxCalculatorServiceGrpc.newStub(channel);
    }

    @AfterAll
    public void stop() throws InterruptedException {
        channel.shutdownNow();
        stopServer();
    }

}
