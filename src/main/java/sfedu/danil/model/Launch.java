package sfedu.danil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private LaunchSite launch_site;
    private Rocket rocket;

    public Launch(Integer flight_number, String launch_date_utc, boolean launch_success,
                  LaunchSite launch_site, Rocket rocket, List<Payload> payloads) {
        this.flight_number = flight_number;
        this.launch_date_utc = launch_date_utc;
        this.launch_success = launch_success;
        this.launch_site = launch_site;
        this.rocket = rocket;
    }

    @JsonIgnore
    public List<Payload> getPayloads() {
        return rocket != null && rocket.getSecond_stage() != null
                ? rocket.getSecond_stage().getPayloads()
                : null;
    }
}
