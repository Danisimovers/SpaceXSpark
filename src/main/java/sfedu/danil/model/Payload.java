package sfedu.danil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    private String payload_id;
    private String payload_type;
    private String orbit;
}
