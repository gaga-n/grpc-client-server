package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.proto.PersonIdRequest;
import org.example.proto.PersonResponse;
import org.example.proto.PersonServiceGrpc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PersonServiceClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8081;

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        unaryCall(channel);
        clientStreamingCall(channel, finishLatch);

        if(!finishLatch.await(15, TimeUnit.SECONDS))
            System.out.println("client streaming call not finished within 15 secs");
    }

    private static void unaryCall(ManagedChannel channel) {
        PersonServiceGrpc.PersonServiceBlockingStub blockingStub = PersonServiceGrpc.newBlockingStub(channel);

        System.out.println("Unary call:-");

        PersonIdRequest request = PersonIdRequest.newBuilder().setId(1).build();

        System.out.println(blockingStub.findPersonWithId(request));
    }

    private static void clientStreamingCall(ManagedChannel channel, CountDownLatch finishLatch) {
        PersonServiceGrpc.PersonServiceStub nonBlockingStub = PersonServiceGrpc.newStub(channel);

        System.out.println("Client streaming call:-");

        StreamObserver<PersonResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(PersonResponse response) {
                System.out.println("Lexicographically smallest person:\n" + response.getPerson());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<PersonIdRequest> requestObserver = nonBlockingStub.findLexicographicallySmallestPerson(responseObserver);

        List.of(2, 1).forEach(id -> {
            System.out.println("sending client stream request with id: " + id);
            requestObserver.onNext(PersonIdRequest.newBuilder().setId(id).build());
        });

        requestObserver.onCompleted();
    }
}
