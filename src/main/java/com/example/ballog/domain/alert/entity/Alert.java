package com.example.ballog.domain.alert.entity;

import com.example.ballog.domain.login.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert", schema = "ballog")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "start_alert", nullable = false)
    private Boolean startAlert= false;

    @Column(name = "in_game_alert", nullable = false)
    private Boolean inGameAlert= false;

}
