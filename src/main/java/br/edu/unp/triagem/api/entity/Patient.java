package br.edu.unp.triagem.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String ticket;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer priority;

    @NotNull
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private LocalDate arrivalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime attendedAt;

    @Builder
    private Patient(
            Long id,
            String name,
            String ticket,
            Integer priority,
            LocalTime arrivalTime,
            LocalDate arrivalDate,
            PatientStatus status,
            LocalTime attendedAt
    ) {
        this.id = id;
        this.name = name;
        this.ticket = ticket;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.arrivalDate = arrivalDate;
        this.status = status;
        this.attendedAt = attendedAt;
    }
}
