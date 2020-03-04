package webclient.fun;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
class RequestMaker {

    private final WebClient webclient;
    private final RestTemplate restTemplate;

    RequestMaker(WebClient webclient, RestTemplate restTemplate) {
        this.webclient = webclient;
        this.restTemplate = restTemplate;
    }

    void makeGetRequest(URI uri) {
        log.info("Making GET reuqest to URI={}", uri);
        final var monoClientResponse = webclient.get().uri(uri).exchange().flatMap(response -> response.bodyToMono(String.class)).doOnSuccess(log::info);
        monoClientResponse.subscribe(response -> {
            log.info("Analysis of body");
        });

        log.info("After GET request to URI={}", uri);
    }

    void makePostRequest(URI uri, byte[] body) {
        /*webclient.post().uri(uri).body(BodyInserters.fromValue(body)).exchange(); */
        final var monoClientResponse = webclient
                .method(HttpMethod.POST)
                .uri(uri)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(body)
                .exchange()
                .block();
    }

    void makePutRequestRetrieve(URI uri) {
        webclient.head()
                .uri(uri).accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToFlux(String.class)
                .blockLast();
    }

    void makePostRequest(URI uri, java.util.List<Integer> body) {
        final var monoClientResponse = webclient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Flux.just(body.toArray(new Integer[0])), Integer.class))
                .exchange()
                .block();

//        monoClientResponse = webclient
//                .post()
//                .uri(uri)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromPublisher(Mono.just(body.toArray(new Integer[0])), Integer.class))
//                .exchange()
//                .block();
    }

    void makePostWithResttemplate(URI uri, byte[] body) {
        byte[] payload = body;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(payload.length);

        HttpEntity entity = new HttpEntity(payload, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);

    }

    void playWithMap() {
        java.util.List.of(1, 2, 3, 4, 5).stream().map(i -> i * 5).flatMap(e -> {
            log.info("Value: {} - {}", e, e % 2);
            if(e % 2 == 0) {
                return Stream.of(e);
            }
            return Stream.empty();
        }).map(Object::toString).forEach(log::info);
        log.info("--- SEPARATOR --");
        List.of(1, 2, 3, 4, 5).map(i -> i * 5).flatMap(e -> {
            log.info("Value: {} - {}", e, e % 2);
            if(e % 2 == 0) {
                return List.of(e);
            }
            return List.empty();
        }).map(Object::toString).forEach(log::info);
    }

}
