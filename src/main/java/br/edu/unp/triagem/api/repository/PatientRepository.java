package br.edu.unp.triagem.api.repository;

import br.edu.unp.triagem.api.entity.Patient;
import br.edu.unp.triagem.api.entity.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Priority Queue ordering: lowest priority number (1 = Emergência) first,
     * arrivalTime ASC as tiebreaker (FIFO within the same level).
     */
    @Query("""
        SELECT p FROM Patient p
        WHERE p.status = 'WAITING'
        ORDER BY p.priority ASC, p.arrivalTime ASC
    """)
    List<Patient> findQueueOrdered();

    @Query("""
        SELECT p FROM Patient p
        WHERE p.status IN ('WAITING', 'IN_SERVICE')
        ORDER BY
            CASE WHEN p.status = 'IN_SERVICE' THEN 0 ELSE 1 END,
            p.priority ASC,
            p.arrivalTime ASC
    """)
    List<Patient> findActiveOrdered();

    Optional<Patient> findFirstByStatus(PatientStatus status);

    List<Patient> findByStatusOrderByAttendedAtDesc(PatientStatus status);

    long countByArrivalDate(LocalDate arrivalDate);
}
