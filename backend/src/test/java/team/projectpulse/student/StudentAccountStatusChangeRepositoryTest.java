package team.projectpulse.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAccountStatusChangeRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    StudentAccountStatusChangeRepository statusChangeRepository;

    @Autowired
    StudentRepository studentRepository;

    @Test
    void findStatusAtWeekBoundaryUsesLatestTimestampThenHighestIdAndExcludesBoundary() {
        Student student = this.studentRepository.findById(4).orElseThrow();
        LocalDateTime boundary = LocalDateTime.of(2026, 10, 5, 0, 0);

        this.statusChangeRepository.save(new StudentAccountStatusChange(student, false, boundary.minusDays(1)));
        this.statusChangeRepository.save(new StudentAccountStatusChange(student, false, boundary.minusHours(1)));
        StudentAccountStatusChange expected = this.statusChangeRepository.save(
                new StudentAccountStatusChange(student, true, boundary.minusHours(1)));
        this.statusChangeRepository.save(new StudentAccountStatusChange(student, false, boundary));
        this.statusChangeRepository.flush();

        StudentAccountStatusChange result = this.statusChangeRepository
                .findFirstByStudentIdAndEffectiveAtBeforeOrderByEffectiveAtDescStatusChangeIdDesc(
                        student.getId(), boundary)
                .orElseThrow();

        assertThat(result.getStatusChangeId()).isEqualTo(expected.getStatusChangeId());
        assertThat(result.isEnabled()).isTrue();
    }
}
