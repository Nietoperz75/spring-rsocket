package pl.grabla.springrsocket;

import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import pl.grabla.springrsocket.dto.ChartResponseDto;
import pl.grabla.springrsocket.dto.ComputationRequestDto;
import pl.grabla.springrsocket.dto.ComputationResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Lec01RSocketTest {

    private RSocketRequester requester;


    @Autowired
    private RSocketRequester.Builder builder;

    @BeforeAll
    public void setup(){
        this.requester = this.builder.transport(TcpClientTransport.create("localhost", 6565));
    }

    @Test
    public void fireAndForget(){
        Mono<Void> mono = this.requester.route("math.service.print")
                .data(new ComputationRequestDto(23))
                .send();

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    public void requestResponse(){
        Mono<ComputationResponseDto> mono = this.requester.route("math.service.square")
                .data(new ComputationRequestDto(12))
                .retrieveMono(ComputationResponseDto.class)
                        .doOnNext(System.out::println);

        Assertions.assertEquals(144, mono.block().getOutput());

        StepVerifier.create(mono).expectNextCount(1).verifyComplete();
    }

    @Test
    public void requestStream(){
        Flux<ComputationResponseDto> flux = this.requester.route("math.service.table")
                .data(new ComputationRequestDto(12))
                .retrieveFlux(ComputationResponseDto.class)
                .doOnNext(System.out::println);

//        Assertions.assertEquals(144, mono.block().getOutput());

        StepVerifier.create(flux).expectNextCount(10).verifyComplete();
    }

    @Test
    public void requestChannel(){
        Flux<ComputationRequestDto> dtoFlux = Flux.range(-10,21).map(ComputationRequestDto::new);
        Flux<ChartResponseDto> flux = this.requester.route("math.service.chart")
                .data(dtoFlux)
                .retrieveFlux(ChartResponseDto.class)
                .doOnNext(System.out::println);

//        Assertions.assertEquals(144, mono.block().getOutput());

        StepVerifier.create(flux).expectNextCount(21).verifyComplete();
    }


}
