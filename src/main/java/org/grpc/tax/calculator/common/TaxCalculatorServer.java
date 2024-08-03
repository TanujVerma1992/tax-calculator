package org.grpc.tax.calculator.common;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class TaxCalculatorServer {

    private final Server server;
    private static TaxCalculatorServer taxCalculatorServer;


    public TaxCalculatorServer(Server server){
        this.server = server;
    }

    public TaxCalculatorServer(int port, BindableService... bindableServices){
       ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
       for(BindableService bindableService : bindableServices){
           log.info("started service : {}",bindableService.bindService().getServiceDescriptor().getName());
           serverBuilder.addService(bindableService);
       }
        server = serverBuilder.build();
    }

    public static TaxCalculatorServer createServer(BindableService... bindableServices){
        taxCalculatorServer = new TaxCalculatorServer(6565, bindableServices);
        return taxCalculatorServer;
    }

    public TaxCalculatorServer(int port, Consumer<NettyServerBuilder> bindableServiceConsumer){
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        bindableServiceConsumer.accept((NettyServerBuilder)serverBuilder);
        server = serverBuilder.build();
    }

    public TaxCalculatorServer start(){
        try {
            server.start();
        } catch (IOException e) {
            log.info("Error : {}",e.getMessage());
            throw new RuntimeException(e);
        }
        return taxCalculatorServer;
    }

    public void await(){
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            log.info("Error : {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        server.shutdownNow();
    }


}
