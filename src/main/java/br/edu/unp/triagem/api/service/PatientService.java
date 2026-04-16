package br.edu.unp.triagem.api.service;

import br.edu.unp.triagem.api.dto.PatientRequest;
import br.edu.unp.triagem.api.entity.Patient;
import br.edu.unp.triagem.api.entity.PatientStatus;
import br.edu.unp.triagem.api.exception.NotFoundException;
import br.edu.unp.triagem.api.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Priority Queue (Max-Heap by urgency) implementation for the Manchester
 * triage protocol. Ordering rules:
 *   1. priority ASC  (1 = Emergência = head of the queue)
 *   2. arrivalTime ASC  (FIFO within the same priority level)
 *
 * The queue is materialized by the database ORDER BY clause — JPA acts as
 * the persistent backing store for the logical heap.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository repository;
    private final Clock clock;


    /** Active queue: current IN_SERVICE (if any) + all WAITING, ordered by priority. */
    @Transactional(readOnly = true)
    public List<Patient> getQueue() {
        log.debug("Buscando fila ativa (WAITING + IN_SERVICE).");
        return repository.findActiveOrdered();
    }

    @Transactional(readOnly = true)
    public List<Patient> getHistory() {
        log.debug("Buscando historico de pacientes atendidos.");
        return repository.findByStatusOrderByAttendedAtDesc(PatientStatus.ATTENDED);
    }

    @Transactional
    public Patient register(PatientRequest req) {
        LocalDate today = LocalDate.now(clock);
        long count = repository.countByArrivalDate(today);
        String ticket = "T" + String.format("%03d", count + 1);

        Patient patient = Patient.builder()
                .name(req.name().trim())
                .ticket(ticket)
                .priority(req.priority())
                .arrivalTime(LocalTime.now(clock).withNano(0))
                .arrivalDate(today)
                .status(PatientStatus.WAITING)
                .build();

        Patient saved = repository.save(patient);
        log.info("Paciente registrado. id={} ticket={} prioridade={}", saved.getId(), saved.getTicket(), saved.getPriority());
        return saved;
    }

    /**
     * Dequeue operation: finish the current IN_SERVICE (if any) and promote
     * the head of the priority queue to IN_SERVICE.
     */
    @Transactional
    public Patient callNext() {
        repository.findFirstByStatus(PatientStatus.IN_SERVICE).ifPresent(current -> {
            current.setStatus(PatientStatus.ATTENDED);
            current.setAttendedAt(LocalTime.now(clock).withNano(0));
            repository.save(current);
            log.info("Paciente finalizado automaticamente ao chamar o proximo. id={}", current.getId());
        });

        List<Patient> waiting = repository.findQueueOrdered();
        if (waiting.isEmpty()) {
            throw new NotFoundException("Fila vazia — nenhum paciente a chamar.");
        }

        Patient next = waiting.get(0);
        next.setStatus(PatientStatus.IN_SERVICE);
        Patient saved = repository.save(next);
        log.info("Proximo paciente chamado. id={} ticket={}", saved.getId(), saved.getTicket());
        return saved;
    }

    @Transactional
    public Patient finish(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente " + id + " não encontrado."));

        if (patient.getStatus() != PatientStatus.IN_SERVICE) {
            throw new IllegalStateException("Paciente não está em atendimento.");
        }

        patient.setStatus(PatientStatus.ATTENDED);
        patient.setAttendedAt(LocalTime.now(clock).withNano(0));
        Patient saved = repository.save(patient);
        log.info("Atendimento finalizado. id={} ticket={}", saved.getId(), saved.getTicket());
        return saved;
    }
}
