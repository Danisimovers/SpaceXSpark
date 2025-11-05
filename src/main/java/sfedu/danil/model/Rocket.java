package sfedu.danil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rocket {
    private String rocket_id;
    private String rocket_name;
    private String rocket_type;
    private SecondStage second_stage;

    public Rocket(String rocket_id, String rocket_name, String rocket_type) {
        this.rocket_id = rocket_id;
        this.rocket_name = rocket_name;
        this.rocket_type = rocket_type;
    }
}
