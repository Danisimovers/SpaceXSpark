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

    @JsonIgnore
    public List<Payload> getPayloads() {
        return rocket != null && rocket.getSecond_stage() != null
                ? rocket.getSecond_stage().getPayloads()
                : null;
    }
}
