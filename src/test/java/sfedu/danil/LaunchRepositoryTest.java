package sfedu.danil;

import org.junit.jupiter.api.*;
import sfedu.danil.model.Launch;
import sfedu.danil.model.Rocket;
import sfedu.danil.model.Payload;
import sfedu.danil.repository.LaunchRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LaunchRepositoryTest {

    private static LaunchRepository repository;
    private static Launch testLaunch1;
    private static Launch testLaunch2;

    @BeforeAll
    static void setup() {
        repository = new LaunchRepository();

        Rocket rocket = new Rocket("falcon9", "Falcon 9", "FT");
        Payload payload = new Payload("payload1", "Satellite", "LEO");

        testLaunch1 = new Launch(1, "2025-10-13T19:00:00Z", true, null, rocket, List.of(payload));
        testLaunch2 = new Launch(2, "2025-10-14T19:00:00Z", false, null, rocket, List.of(payload));
    }

    @Test
    @Order(1)
    void testSaveAllAndReadAll() {
        repository.saveAll(List.of(testLaunch1, testLaunch2));
        List<Launch> launches = repository.readAll();
        assertEquals(2, launches.size(), "Должно быть 2 запуска после saveAll");
    }

    @Test
    @Order(2)
    void testReadByFlightNumber() {
        Optional<Launch> launch = repository.readByFlightNumber(1);
        assertTrue(launch.isPresent(), "Запуск с flight_number 1 должен существовать");
        assertEquals(testLaunch1.getFlight_number(), launch.get().getFlight_number());
    }

    @Test
    @Order(3)
    void testExistsByFlightNumber() {
        assertTrue(repository.existsByFlightNumber(1), "Запуск с flight_number 1 должен существовать");
        assertFalse(repository.existsByFlightNumber(999), "Запуск с flight_number 999 не должен существовать");
    }

    @Test
    @Order(4)
    void testSaveByFlightNumber_Add() {
        Launch newLaunch = new Launch(3, "2025-10-15T19:00:00Z", true, null, testLaunch1.getRocket(), testLaunch1.getPayloads());
        repository.saveByFlightNumber(newLaunch);
        Optional<Launch> saved = repository.readByFlightNumber(3);
        assertTrue(saved.isPresent(), "Новый запуск с flight_number 3 должен быть добавлен");
    }

    @Test
    @Order(5)
    void testSaveByFlightNumber_Update() {
        Launch updatedLaunch = new Launch(1, "2025-10-13T20:00:00Z", false, null, testLaunch1.getRocket(), testLaunch1.getPayloads());
        repository.saveByFlightNumber(updatedLaunch);
        Optional<Launch> launch = repository.readByFlightNumber(1);
        assertTrue(launch.isPresent());
        assertEquals("2025-10-13T20:00:00Z", launch.get().getLaunch_date_utc());
        assertFalse(launch.get().isLaunch_success());
    }

    @Test
    @Order(6)
    void testUpdateLaunch() {
        Launch updatedLaunch = new Launch(2, "2025-10-14T20:00:00Z", true, null, testLaunch2.getRocket(), testLaunch2.getPayloads());
        repository.updateLaunch(2, updatedLaunch);
        Optional<Launch> launch = repository.readByFlightNumber(2);
        assertTrue(launch.isPresent());
        assertEquals("2025-10-14T20:00:00Z", launch.get().getLaunch_date_utc());
        assertTrue(launch.get().isLaunch_success());
    }

    @Test
    @Order(7)
    void testDeleteLaunch() {
        repository.deleteLaunch(1);
        Optional<Launch> launch = repository.readByFlightNumber(1);
        assertFalse(launch.isPresent(), "Запуск с flight_number 1 должен быть удален");
        assertEquals(2, repository.readAll().size(), "Должно остаться 2 запуска после удаления");
    }

    @Test
    @Order(8)
    void testDeleteAll() {
        repository.deleteAll();
        assertEquals(0, repository.readAll().size(), "Все запуски должны быть удалены");
    }
}
