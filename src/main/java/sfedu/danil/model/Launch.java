package sfedu.danil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Launch {
    private Integer flight_number;
    private String launch_date_utc;
    private boolean launch_success;
    private String site_name;
    private Rocket rocket;
    private List<Payload> payloads;
}
