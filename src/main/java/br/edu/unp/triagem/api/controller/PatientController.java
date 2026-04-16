package br.edu.unp.triagem.api.controller;

import br.edu.unp.triagem.api.dto.PatientRequest;
import br.edu.unp.triagem.api.dto.PatientResponse;
import br.edu.unp.triagem.api.entity.Patient;
import br.edu.unp.triagem.api.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService service;


    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest req) {
        log.info("Registrando novo paciente na fila. prioridade={}", req.priority());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.register(req)));
    }

    @GetMapping("/queue")
    public List<PatientResponse> queue() {
        log.debug("Consultando fila ativa de pacientes.");
        return service.getQueue().stream().map(PatientController::toResponse).toList();
    }

    @PostMapping("/next")
    public PatientResponse callNext() {
        log.info("Solicitacao para chamar proximo paciente.");
        return toResponse(service.callNext());
    }

    @PostMapping("/{id}/finish")
    public PatientResponse finish(@PathVariable Long id) {
        log.info("Solicitacao para finalizar atendimento do paciente id={}", id);
        return toResponse(service.finish(id));
    }

    @GetMapping("/history")
    public List<PatientResponse> history() {
        log.debug("Consultando historico de atendimentos.");
        return service.getHistory().stream().map(PatientController::toResponse).toList();
    }

    private static PatientResponse toResponse(Patient patient) {
        return PatientResponse.from(patient);
    }
}
