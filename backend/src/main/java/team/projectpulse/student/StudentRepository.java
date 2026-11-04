package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    // Find all students in a section
    List<Student> findBySectionSectionId(Integer sectionId);

    List<Student> findByTeamTeamId(Integer teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Student> findByIdAndSectionCourseCourseAdminId(Integer studentId, Integer courseAdminId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Student> findByIdAndSectionInstructorsId(Integer studentId, Integer instructorId);

    boolean existsByIdAndSectionCourseCourseAdminId(Integer studentId, Integer courseAdminId);

    boolean existsByIdAndSectionInstructorsId(Integer studentId, Integer instructorId);

    Optional<Student> findFirstByIdAndSectionCourseCourseAdminId(Integer studentId, Integer courseAdminId);

    Optional<Student> findFirstByIdAndSectionInstructorsId(Integer studentId, Integer instructorId);
}
