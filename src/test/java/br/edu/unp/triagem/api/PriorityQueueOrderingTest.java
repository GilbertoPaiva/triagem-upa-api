package br.edu.unp.triagem.api;

import br.edu.unp.triagem.api.dto.PatientRequest;
import br.edu.unp.triagem.api.entity.Patient;
import br.edu.unp.triagem.api.repository.PatientRepository;
import br.edu.unp.triagem.api.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class PriorityQueueOrderingTest {

    @Autowired
    private PatientService service;

    @Autowired
    private PatientRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void emergencyPatientJumpsToTheFront() throws InterruptedException {
        service.register(new PatientRequest("Paula (P4)", 4));
        Thread.sleep(1100);
        service.register(new PatientRequest("Roberto (P5)", 5));
        Thread.sleep(1100);
        service.register(new PatientRequest("Ana (P3)", 3));
        Thread.sleep(1100);
        service.register(new PatientRequest("João (P1)", 1));

        List<Patient> queue = service.getQueue();

        assertThat(queue).hasSize(4);
        assertThat(queue.get(0).getName()).isEqualTo("João (P1)");
        assertThat(queue.get(1).getName()).isEqualTo("Ana (P3)");
        assertThat(queue.get(2).getName()).isEqualTo("Paula (P4)");
        assertThat(queue.get(3).getName()).isEqualTo("Roberto (P5)");
    }

    @Test
    void samePriorityRespectsFifoArrivalOrder() throws InterruptedException {
        service.register(new PatientRequest("Primeiro P3", 3));
        Thread.sleep(1100);
        service.register(new PatientRequest("Segundo P3", 3));
        Thread.sleep(1100);
        service.register(new PatientRequest("Terceiro P3", 3));

        List<Patient> queue = service.getQueue();

        assertThat(queue).extracting(Patient::getName)
                .containsExactly("Primeiro P3", "Segundo P3", "Terceiro P3");
    }
}
