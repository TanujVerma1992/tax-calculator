package org.grpc.tax.calculator.services;

import com.grpc.tax.calculator.models.TaxBifurcation;
import com.grpc.tax.calculator.models.TaxCalculatorServiceGrpc.TaxCalculatorServiceImplBase;
import com.grpc.tax.calculator.models.TaxDetailedBifurcation;
import com.grpc.tax.calculator.models.TotalIncomeRequest;
import io.grpc.stub.StreamObserver;
import org.grpc.tax.calculator.Utils.DecimalUtils;
import org.grpc.tax.calculator.common.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class TaxCalculatorService extends TaxCalculatorServiceImplBase {

    public static final Double STANDARD_DEDUCTION = 75000.00;

    @Override
    public void calculateTax(TotalIncomeRequest request, StreamObserver<TaxBifurcation> responseObserver) {
        var taxAmount = taxAmount(request.getTotalIncome());
        var response = TaxBifurcation.newBuilder().setTax(taxAmount).setTaxPercentage((float) (taxAmount / request.getTotalIncome())).setStartRange(0).setEndRange(request.getTotalIncome()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void calculateDetailedTax(TotalIncomeRequest request, StreamObserver<TaxDetailedBifurcation> responseObserver) {
        responseObserver.onNext(consolidatedTaxAmount(request.getTotalIncome()));
        responseObserver.onCompleted();
    }

    private double taxAmount(double amount) {
        var totalTax = 0.0;
        totalTax += componentCalculator(amount, ComponentCategory.SLAB_1);
        totalTax += componentCalculator(amount, ComponentCategory.SLAB_2);
        totalTax += componentCalculator(amount, ComponentCategory.SLAB_3);
        totalTax += componentCalculator(amount, ComponentCategory.SLAB_4);
        totalTax += componentCalculator(amount, ComponentCategory.SLAB_5);
        /*if(amount<=7* MoneyUtils.ONE_LAKH){
            totalTax+=(((amount/MoneyUtils.ONE_LAKH)-3)* MoneyUtils.ONE_LAKH*0.05);
        }
        if(amount<=10*MoneyUtils.ONE_LAKH){
            totalTax+=(((amount/MoneyUtils.ONE_LAKH) - 7)*MoneyUtils.ONE_LAKH*.10);
        }
        if(amount<=12*MoneyUtils.ONE_LAKH ){
            totalTax+=(((amount/MoneyUtils.ONE_LAKH) - 10)*MoneyUtils.ONE_LAKH*.15);
        }
        if(amount<=15*MoneyUtils.ONE_LAKH ){
            totalTax+=(((amount/MoneyUtils.ONE_LAKH) - 12)*MoneyUtils.ONE_LAKH*.20);
        }if(amount>15*MoneyUtils.ONE_LAKH){
            totalTax+=(((amount/MoneyUtils.ONE_LAKH) - 15)*MoneyUtils.ONE_LAKH*.30);
        }*/
        return totalTax;
    }

    private TaxDetailedBifurcation consolidatedTaxAmount(double amount) {
        List<TaxBifurcation> TaxBifurcations = new ArrayList<>();
        if (amount > 0) {
            TaxBifurcations.add(bifuricatedComponentCalculator(amount, ComponentCategory.SLAB_1));
        }
        amount -= STANDARD_DEDUCTION;
        if (amount > 3 * MoneyUtils.ONE_LAKH) {
            TaxBifurcations.add(bifuricatedComponentCalculator(amount, ComponentCategory.SLAB_2));
        }
        if (amount > 7 * MoneyUtils.ONE_LAKH) {
            TaxBifurcations.add(bifuricatedComponentCalculator(amount, ComponentCategory.SLAB_3));
        }
        if (amount > 10 * MoneyUtils.ONE_LAKH) {
            TaxBifurcations.add(bifuricatedComponentCalculator(amount, ComponentCategory.SLAB_4));
        }
        if (amount > 12 * MoneyUtils.ONE_LAKH) {
            TaxBifurcations.add(bifuricatedComponentCalculator(amount, ComponentCategory.SLAB_5));
        }
        var totalTax = TaxBifurcations.stream().mapToDouble(TaxBifurcation::getTax).sum();
        var inHandSalary = amount - totalTax;
        var taxAgainstIncome = (totalTax / amount) * 100;
        return TaxDetailedBifurcation.newBuilder().addAllTaxDetails(TaxBifurcations)
                .setTotalTax(totalTax).setInHandSalary(inHandSalary)
                .setTaxAgainstIncome(DecimalUtils.formatToPrecision(taxAgainstIncome)+"%")
                .setStandardDeduction(STANDARD_DEDUCTION).build();
    }

    private double componentCalculator(double amount, ComponentCategory componentCategory) {
        double taxAmount = 0.0;
        switch (componentCategory) {
            case SLAB_1 -> taxAmount = 0.0;
            case SLAB_2 -> taxAmount = (amount >= 3 * MoneyUtils.ONE_LAKH && amount < 7 * MoneyUtils.ONE_LAKH) ?
                    (amount - 3 * MoneyUtils.ONE_LAKH) * 0.05 : (amount >= 7 * MoneyUtils.ONE_LAKH) ?
                    4 * MoneyUtils.ONE_LAKH * 0.05 : 0.0;
            case SLAB_3 -> taxAmount = (amount >= 7 * MoneyUtils.ONE_LAKH && amount < 10 * MoneyUtils.ONE_LAKH) ?
                    (amount - 7 * MoneyUtils.ONE_LAKH) * 0.10 : (amount >= 10 * MoneyUtils.ONE_LAKH) ?
                    3 * MoneyUtils.ONE_LAKH * 0.10 : 0.0;
            case SLAB_4 -> taxAmount = (amount >= 10 * MoneyUtils.ONE_LAKH && amount < 12 * MoneyUtils.ONE_LAKH) ?
                    (amount - 10 * MoneyUtils.ONE_LAKH) * 0.20 : (amount >= 12 * MoneyUtils.ONE_LAKH) ?
                    2 * MoneyUtils.ONE_LAKH * 0.20 : 0.0;
            case SLAB_5 -> taxAmount = (amount >= 12 * MoneyUtils.ONE_LAKH && amount < 15 * MoneyUtils.ONE_LAKH) ?
                    (amount - 12 * MoneyUtils.ONE_LAKH) * 0.30 : (amount >= 15 * MoneyUtils.ONE_LAKH) ?
                    (amount - 15 * MoneyUtils.ONE_LAKH) * 0.30 : 0.0;
            case null, default -> throw new RuntimeException("Invalid Slab Received.");
        }
        return taxAmount;
    }

    private TaxBifurcation bifuricatedComponentCalculator(double amount, ComponentCategory componentCategory) {
        double taxAmount = 0.0;
        switch (componentCategory) {
            case SLAB_1 -> {
                return TaxBifurcation.newBuilder().setTax(taxAmount).setEndRange(3 * MoneyUtils.ONE_LAKH)
                        .setStartRange(0).setTaxPercentage(0).build();
            }
            case SLAB_2 -> {
                var endRange = (amount >= 3 * MoneyUtils.ONE_LAKH && amount < 7 * MoneyUtils.ONE_LAKH) ?
                        amount : (amount >= 7 * MoneyUtils.ONE_LAKH) ? 7 * MoneyUtils.ONE_LAKH : 0.0;
                taxAmount = (amount >= 3 * MoneyUtils.ONE_LAKH && amount < 7 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(5, (amount - 3 * MoneyUtils.ONE_LAKH))
                        : (amount >= 7 * MoneyUtils.ONE_LAKH) ? percentageCalculator(5,
                        (4 * MoneyUtils.ONE_LAKH)) : 0.0;
                return TaxBifurcation.newBuilder().setTax(taxAmount).setEndRange(endRange)
                        .setStartRange(3 * MoneyUtils.ONE_LAKH).setTaxPercentage(5).build();
            }
            case SLAB_3 -> {
                var endRange = (amount >= 7 * MoneyUtils.ONE_LAKH && amount < 10 * MoneyUtils.ONE_LAKH) ?
                        amount : (amount >= 10 * MoneyUtils.ONE_LAKH) ? 10 * MoneyUtils.ONE_LAKH : 0.0;
                taxAmount = (amount >= 7 * MoneyUtils.ONE_LAKH && amount < 10 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(10, (amount - 7 * MoneyUtils.ONE_LAKH))
                        : (amount >= 10 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(10, 3 * MoneyUtils.ONE_LAKH) : 0.0;
                return TaxBifurcation.newBuilder().setTax(taxAmount).setEndRange(endRange)
                        .setStartRange(7 * MoneyUtils.ONE_LAKH).setTaxPercentage(10).build();
            }
            case SLAB_4 -> {
                var endRange = (amount >= 10 * MoneyUtils.ONE_LAKH && amount < 12 * MoneyUtils.ONE_LAKH) ?
                        amount : (amount >= 12 * MoneyUtils.ONE_LAKH) ? 12 * MoneyUtils.ONE_LAKH : 0.0;
                taxAmount = (amount >= 10 * MoneyUtils.ONE_LAKH && amount < 12 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(20, (amount - 10 * MoneyUtils.ONE_LAKH))
                        : (amount >= 12 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(20, 2 * MoneyUtils.ONE_LAKH) : 0.0;
                return TaxBifurcation.newBuilder().setTax(taxAmount).setEndRange(endRange)
                        .setStartRange(10 * MoneyUtils.ONE_LAKH).setTaxPercentage(20).build();
            }
            case SLAB_5 -> {
                var endRange = (amount >= 12 * MoneyUtils.ONE_LAKH && amount < 15 * MoneyUtils.ONE_LAKH) ?
                        amount : (amount >= 15 * MoneyUtils.ONE_LAKH) ? 15 * MoneyUtils.ONE_LAKH : 0.0;
                taxAmount = (amount >= 12 * MoneyUtils.ONE_LAKH && amount < 15 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(30, (amount - 12 * MoneyUtils.ONE_LAKH))
                        : (amount >= 15 * MoneyUtils.ONE_LAKH) ?
                        percentageCalculator(30, amount - 15 * MoneyUtils.ONE_LAKH) : 0.0;
                return TaxBifurcation.newBuilder().setTax(taxAmount).setEndRange(endRange)
                        .setStartRange(12 * MoneyUtils.ONE_LAKH).setTaxPercentage(30).build();
            }
            case null, default -> throw new RuntimeException("Invalid Slab Received.");
        }
    }

    private double percentageCalculator(int percentage, double amount) {
        return amount * 0.01 * percentage;
    }

    private enum ComponentCategory {
        SLAB_1, SLAB_2, SLAB_3, SLAB_4, SLAB_5
    }
}
