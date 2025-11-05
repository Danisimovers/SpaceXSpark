package sfedu.danil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayloadEntity {
    @Id
    @Column(name = "payload_id")
    private String payload_id;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "payload_type")
    private String payload_type;

    @Column(name = "orbit")
    private String orbit;

    @ManyToOne
    @JoinColumn(name = "second_stage_id")
    private SecondStageEntity second_stage;
}