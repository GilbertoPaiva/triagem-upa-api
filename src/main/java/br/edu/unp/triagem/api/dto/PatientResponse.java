package br.edu.unp.triagem.api.dto;

import br.edu.unp.triagem.api.entity.Patient;
import br.edu.unp.triagem.api.entity.PatientStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record PatientResponse(
        Long id,
        String name,
        String ticket,
        Integer priority,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime arrivalTime,
        PatientStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime attendedAt
) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(
                p.getId(),
                p.getName(),
                p.getTicket(),
                p.getPriority(),
                p.getArrivalTime(),
                p.getStatus(),
                p.getAttendedAt()
        );
    }
}
