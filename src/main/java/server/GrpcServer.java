package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    private static final int PORT = 8081;

    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new PersonServiceImpl())
                .build();

        System.out.println("Server starting at port: " + PORT);
        server.start();
        server.awaitTermination();
    }
}
