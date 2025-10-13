package sfedu.danil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sfedu.danil.model.Launch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SpaceXService {

    private static final Logger logger = LogManager.getLogger(SpaceXService.class);

    private static final String API_URL = "https://api.spacexdata.com/v3/launches";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SpaceXService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }


    public List<Launch> getAllLaunches() throws IOException, InterruptedException {
        logger.info("Отправка запроса к SpaceX API: {}", API_URL);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(API_URL))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("Ошибка при получении данных с API: статус {}", response.statusCode());
            throw new IOException("Ошибка при получении данных: " + response.statusCode());
        }

        // Парсим JSON в список моделей Launch
        List<Launch> launches = objectMapper.readValue(response.body(), new TypeReference<List<Launch>>() {});
        logger.info("Получено {} запусков", launches.size());

        if (launches.isEmpty()) {
            logger.warn("API вернуло пустой список запусков");
        }

        return launches;
    }
}
