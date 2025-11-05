package sfedu.danil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "launch_sites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaunchSiteEntity {
    @Id
    @Column(name = "site_id")
    private String site_id;

    @Column(name = "site_name")
    private String site_name;

    @Column(name = "site_name_long")
    private String site_name_long;
}