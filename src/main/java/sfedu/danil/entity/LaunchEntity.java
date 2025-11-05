package sfedu.danil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "launches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaunchEntity {
    @Id
    @Column(name = "flight_number")
    private Integer flight_number;

    @Column(name = "launch_date_utc")
    private String launch_date_utc;

    @Column(name = "launch_success")
    private boolean launch_success;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "launch_site_id")
    private LaunchSiteEntity launch_site;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rocket_id")
    private RocketEntity rocket;
}