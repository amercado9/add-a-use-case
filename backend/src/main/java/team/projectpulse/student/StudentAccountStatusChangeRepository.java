package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StudentAccountStatusChangeRepository extends JpaRepository<StudentAccountStatusChange, Integer> {

    Optional<StudentAccountStatusChange> findFirstByStudentIdAndEffectiveAtBeforeOrderByEffectiveAtDescStatusChangeIdDesc(
            Integer studentId,
            LocalDateTime boundary
    );
}
