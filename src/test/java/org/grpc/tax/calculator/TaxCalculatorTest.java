package org.grpc.tax.calculator;

import com.grpc.tax.calculator.models.TaxBifurcation;
import com.grpc.tax.calculator.models.TaxDetailedBifurcation;
import com.grpc.tax.calculator.models.TotalIncomeRequest;
import org.grpc.tax.calculator.common.AbstractChannelTest;
import org.grpc.tax.calculator.common.MoneyUtils;
import org.grpc.tax.calculator.common.ResponseObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class TaxCalculatorTest extends AbstractChannelTest {

    @Test
    public void taxCalculationTest(){
        var request = TotalIncomeRequest.newBuilder().setTotalIncome(10* MoneyUtils.ONE_LAKH).setYear(2024).build();
        var responseObserver = new ResponseObserver<TaxBifurcation>();
        taxCalculatorServiceStub.calculateTax(request, responseObserver);
        responseObserver.await();
        Assertions.assertEquals(1,responseObserver.getResult().size());
    }

    @Test
    public void detailedTaxCalculationTest(){
        var request = TotalIncomeRequest.newBuilder().setTotalIncome(10* MoneyUtils.ONE_LAKH).setYear(2024).build();
        var responseObserver = new ResponseObserver<TaxDetailedBifurcation>();
        taxCalculatorServiceStub.calculateDetailedTax(request, responseObserver);
        responseObserver.await();
        Assertions.assertEquals(3,responseObserver.getResult().getFirst().getTaxDetailsList().size());
    }
}
