package org.grpc.tax.calculator.common;

import org.grpc.tax.calculator.services.TaxCalculatorService;

public abstract class AbstractTest {

    private TaxCalculatorServer taxCalculatorServer;

    protected void initiateServer(){
        taxCalculatorServer = TaxCalculatorServer.createServer(new TaxCalculatorService()).start();
    //    taxCalculatorServer.await();
    }


    protected void stopServer(){
        taxCalculatorServer.stop();
    }

}
