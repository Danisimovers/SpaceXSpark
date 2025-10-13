package sfedu.danil;

import org.junit.jupiter.api.Test;
import sfedu.danil.model.Launch;
import sfedu.danil.service.SpaceXService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpaceXServiceTest {

    @Test
    public void testGetAllLaunches() {
        SpaceXService service = new SpaceXService();

        try {
            List<Launch> launches = service.getAllLaunches();

            assertNotNull(launches, "Список запусков не должен быть null");
            assertFalse(launches.isEmpty(), "Список запусков не должен быть пустым");

            Launch firstLaunch = launches.get(0);
            assertNotNull(firstLaunch.getFlight_number(), "flight_id первого запуска не должен быть null");

        } catch (IOException | InterruptedException e) {
            fail("Ошибка при вызове SpaceX API: " + e.getMessage());
        }
    }
}
