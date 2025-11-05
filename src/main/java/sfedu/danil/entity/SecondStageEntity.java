package sfedu.danil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "second_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecondStageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block")
    private Integer block;

    @OneToMany(mappedBy = "second_stage", cascade = CascadeType.ALL)
    private List<PayloadEntity> payloads;
}