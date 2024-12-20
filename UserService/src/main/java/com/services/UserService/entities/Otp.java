package com.services.UserService.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name="otps")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fpid;

    @Column(nullable = false)
    private Integer otpValue;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne
    private User user;
}
