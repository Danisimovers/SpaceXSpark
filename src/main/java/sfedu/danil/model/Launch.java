package sfedu.danil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Launch {
    private String flight_id;
    private String launch_date_utc;
    private boolean launch_success;
    private String site_name;
    private Rocket rocket;
    private List<Payload> payloads;
}
