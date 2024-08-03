package org.grpc.tax.calculator.common;

import org.grpc.tax.calculator.services.TaxCalculatorService;

public class TaxCalculatorApplication {

    public static void main(String[] args){
            TaxCalculatorServer.createServer(new TaxCalculatorService()).start().await();
    }
}
