package sfedu.danil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {
    private String payload_id;
    private String nationality;
    private String payload_type;
    private String orbit;

    public Payload(String payload_id, String payload_type, String orbit) {
        this.payload_id = payload_id;
        this.payload_type = payload_type;
        this.orbit = orbit;
    }
}
