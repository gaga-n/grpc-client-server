package server;

import io.grpc.stub.StreamObserver;
import org.example.proto.*;

import java.util.List;
import java.util.Map;

public class PersonServiceImpl extends PersonServiceGrpc.PersonServiceImplBase {

    static Map<Integer, Person> personMap = Map.of(
            1, Person.newBuilder()
                    .setId(1)
                    .setFirstName("abc")
                    .setBirthPlaceRegion(Region.EAST_REGION)
                    .setDateOfBirth(
                            Date.newBuilder()
                            .setDay(12)
                            .setMonth(5)
                            .setYear(2000)
                            .build()
                    )
                    .addAllPhoneNumbers(List.of("123", "456"))
                    .build(),
            2, Person.newBuilder()
                    .setId(2)
                    .setFirstName("def")
                    .setBirthPlaceRegion(Region.NORTH_REGION)
                    .setDateOfBirth(
                            Date.newBuilder()
                                    .setDay(15)
                                    .setMonth(7)
                                    .setYear(2001)
                                    .build()
                    )
                    .addAllPhoneNumbers(List.of("789", "012"))
                    .build()
    );

    @Override
    public void findPersonWithId(PersonIdRequest request, StreamObserver<PersonResponse> responseObserver) {
        int requestId = request.getId();

        System.out.println("[findPersonWithId] request for person with id: " + requestId);

        Person requestedPerson = personMap.get(requestId);

        PersonResponse response = PersonResponse.newBuilder().setPerson(requestedPerson).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<PersonIdRequest> findLexicographicallySmallestPerson(StreamObserver<PersonResponse> responseObserver) {
        return new StreamObserver<>() {

            Person lexicographicallySmallestPerson = null;

            @Override
            public void onNext(PersonIdRequest request) {
                System.out.println("[findLexicographicallySmallestPerson] request for person with id: " + request.getId());

                Person requestedPerson = personMap.get(request.getId());

                if(lexicographicallySmallestPerson == null ||
                    lexicographicallySmallestPerson.getFirstName().compareTo(requestedPerson.getFirstName()) > 0)
                    lexicographicallySmallestPerson = requestedPerson;
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(PersonResponse.newBuilder()
                        .setPerson(lexicographicallySmallestPerson)
                        .build());
            }
        };
    }
}
