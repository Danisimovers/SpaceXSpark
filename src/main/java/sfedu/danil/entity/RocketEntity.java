package sfedu.danil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rockets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketEntity {
    @Id
    @Column(name = "rocket_id")
    private String rocket_id;

    @Column(name = "rocket_name")
    private String rocket_name;

    @Column(name = "rocket_type")
    private String rocket_type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "second_stage_id")
    private SecondStageEntity second_stage;
}