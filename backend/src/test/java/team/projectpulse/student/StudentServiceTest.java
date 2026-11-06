package team.projectpulse.student;

import org.junit.jupiter.api.AfterEach;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.user.MyUserPrincipal;
import team.projectpulse.user.userinvitation.UserInvitationService;
import team.projectpulse.system.exception.StudentAlreadyDeactivatedException;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    StudentRepository studentRepository;
    @Mock
    SectionRepository sectionRepository;
    @Mock
    UserInvitationService userInvitationService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserUtils userUtils;
    @Mock
    StudentAccountStatusChangeRepository statusChangeRepository;
    @Mock
    Clock clock;

    @InjectMocks
    private StudentService studentService;

    List<Student> students;

    @BeforeEach
    void setUp() {
        // Create students
        Student john = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        Student eric = new Student("eric", "Eric", "Hudson", "e.hudson@abc.edu", "123456", true, "student");
        Student jerry = new Student("jerry", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");
        Student woody = new Student("woody", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");
        Student amanda = new Student("amanda", "Amanda", "Wagner", "a.wagner@abc.edu", "123456", true, "student");
        Student cora = new Student("cora", "Cora", "Manning", "c.manning@abc.edu", "123456", true, "student");
        Student agustin = new Student("agustin", "Agustin", "Freeman", "a.freeman@abc.edu", "123456", true, "student");
        Student mavis = new Student("mavis", "Mavis", "Huber", "m.huber@abc.edu", "123456", true, "student");
        Student mary = new Student("mary", "Mary", "Vargas", "m.vargas@abc.edu", "123456", true, "student");
        Student rosendo = new Student("rosendo", "Rosendo", "Maxwell", "r.maxwell@abc.edu", "123456", true, "student");

        this.students = List.of(john, eric, jerry, woody, amanda, cora, agustin, mavis, mary, rosendo);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Clear the security context after each test to avoid interference between tests
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("firstName", "john");
        PageImpl expectedPage = new PageImpl(List.of(this.students.get(0)));

        given(this.userUtils.getUserSectionId()).willReturn(2);
        given(this.studentRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Student> result = this.studentService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void testFindStudentById() {
        // Given
        Student john = this.students.get(0);
        given(this.userUtils.getUserId()).willReturn(1);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john));

        // When
        Student result = this.studentService.findStudentById(1);

        // Then
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void owningCourseAdminFindsStudentWithoutSectionAssignment() {
        Student john = this.students.get(0);
        given(this.userUtils.getUserId()).willReturn(20);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(true);
        given(this.studentRepository.findFirstByIdAndSectionCourseCourseAdminId(1, 20))
                .willReturn(Optional.of(john));

        assertThat(this.studentService.findStudentById(1)).isSameAs(john);
    }

    @Test
    void unrelatedInstructorCannotFindStudentThroughUnscopedId() {
        given(this.userUtils.getUserId()).willReturn(21);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(false);
        given(this.studentRepository.findFirstByIdAndSectionInstructorsId(1, 21))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> this.studentService.findStudentById(1))
                .isInstanceOf(ObjectNotFoundException.class);
        verify(this.studentRepository, never()).findById(1);
    }

    @Test
    void testSaveStudent() {
        // Given
        Student jan = new Student("jan", "Jan", "Mckinney", "j.mckinney@abc.edu", "Abc123456", true, "student");
        doNothing().when(this.userInvitationService).validateUserInvitation(anyString(), anyString(), anyInt(), anyInt(), anyString());
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(new Section()));
        given(this.passwordEncoder.encode(anyString())).willReturn("Encoded Password");
        given(this.studentRepository.save(jan)).willReturn(jan);
        given(this.clock.instant()).willReturn(Instant.parse("2026-11-04T18:00:00Z"));
        given(this.clock.getZone()).willReturn(ZoneId.of("America/Chicago"));

        // When
        Student result = this.studentService.saveStudent(jan, 1, 1, "token", "student");

        // Then
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Mckinney");
        assertThat(result.getEmail()).isEqualTo("j.mckinney@abc.edu");
        assertThat(result.getRoles()).isEqualTo("student");
        assertThat(result.getSection()).isNotNull();
        assertThat(result.isEnabled()).isEqualTo(true);
        verify(this.statusChangeRepository).save(argThat(change ->
                change.getStudent() == jan
                        && change.isEnabled()
                        && change.getEffectiveAt().equals(LocalDateTime.of(2026, 11, 4, 12, 0))));
    }

    @Test
    void testEvaluationEligibilityUsesLastStatusStrictlyBeforeFollowingMonday() {
        StudentAccountStatusChange active = new StudentAccountStatusChange(this.students.get(0), true,
                LocalDateTime.of(2026, 10, 4, 23, 59));
        given(this.statusChangeRepository
                .findFirstByStudentIdAndEffectiveAtBeforeOrderByEffectiveAtDescStatusChangeIdDesc(
                        1, LocalDateTime.of(2026, 10, 5, 0, 0)))
                .willReturn(Optional.of(active));

        EvaluationEligibility result = this.studentService.isEvaluationEligible(1, "2026-W40");

        assertThat(result).isEqualTo(EvaluationEligibility.ELIGIBLE);
    }

    @Test
    void testEvaluationEligibilityIsIneligibleForDisabledBoundaryStatus() {
        StudentAccountStatusChange inactive = new StudentAccountStatusChange(this.students.get(0), false,
                LocalDateTime.of(2026, 10, 4, 12, 0));
        given(this.statusChangeRepository
                .findFirstByStudentIdAndEffectiveAtBeforeOrderByEffectiveAtDescStatusChangeIdDesc(anyInt(), any(LocalDateTime.class)))
                .willReturn(Optional.of(inactive));

        EvaluationEligibility result = this.studentService.isEvaluationEligible(1, "2026-W40");

        assertThat(result).isEqualTo(EvaluationEligibility.INELIGIBLE);
    }

    @Test
    void testEvaluationEligibilityIsUnknownWithoutRecordedStatus() {
        given(this.statusChangeRepository
                .findFirstByStudentIdAndEffectiveAtBeforeOrderByEffectiveAtDescStatusChangeIdDesc(anyInt(), any(LocalDateTime.class)))
                .willReturn(Optional.empty());

        EvaluationEligibility result = this.studentService.isEvaluationEligible(1, "2026-W40");

        assertThat(result).isEqualTo(EvaluationEligibility.UNKNOWN);
    }

    @Test
    void testStudentUpdateOwnInfo() {
        // Given
        Student update = new Student("john", "John (updated)", "Smith (updated)", "j.smith@abc.edu (updated)", "", false, "admin student");
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        given(this.studentRepository.save(any(Student.class))).willReturn(this.students.get(0));

        Instructor instructor = new Instructor();
        instructor.setRoles("student");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Student result = this.studentService.updateStudent(1, update);

        // Then
        assertThat(result.getUsername()).isEqualTo(update.getUsername());
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getLastName()).isEqualTo(update.getLastName());
        assertThat(result.getEmail()).isEqualTo(update.getEmail());
        assertThat(result.getRoles()).isNotEqualTo(update.getRoles());
        assertThat(result.isEnabled()).isTrue();
        verify(this.statusChangeRepository, never()).save(any());
    }

    @Test
    void testInstructorUpdatesStudent() {
        // Given
        Student update = new Student("john", "John (updated)", "Smith (updated)", "j.smith@abc.edu (updated)", "", false, "admin student");
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        given(this.studentRepository.save(any(Student.class))).willReturn(this.students.get(0));

        Instructor instructor = new Instructor();
        instructor.setRoles("instructor");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Student result = this.studentService.updateStudent(1, update);

        // Then
        assertThat(result.getUsername()).isEqualTo(update.getUsername());
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getLastName()).isEqualTo(update.getLastName());
        assertThat(result.getEmail()).isEqualTo(update.getEmail());
//        assertThat(result.getRoles()).isEqualTo(update.getRoles());
        assertThat(result.isEnabled()).isTrue();
        verify(this.statusChangeRepository, never()).save(any());
    }

    @Test
    void testCourseAdminDeactivatesStudentAndRecordsTransition() {
        Student student = this.students.get(0);
        student.setId(1);
        given(this.userUtils.getUserId()).willReturn(20);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(true);
        given(this.studentRepository.findByIdAndSectionCourseCourseAdminId(1, 20)).willReturn(Optional.of(student));
        given(this.studentRepository.save(student)).willReturn(student);
        given(this.clock.instant()).willReturn(Instant.parse("2026-11-04T18:00:00Z"));
        given(this.clock.getZone()).willReturn(ZoneId.of("America/Chicago"));

        Student result = this.studentService.deactivateStudent(1);

        assertThat(result.isEnabled()).isFalse();
        verify(this.statusChangeRepository).save(argThat(change ->
                change.getStudent() == student
                        && !change.isEnabled()
                        && change.getEffectiveAt().equals(LocalDateTime.of(2026, 11, 4, 12, 0))));
    }

    @Test
    void testAssignedInstructorDeactivatesStudent() {
        Student student = this.students.get(0);
        student.setId(1);
        given(this.userUtils.getUserId()).willReturn(21);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(false);
        given(this.studentRepository.findByIdAndSectionInstructorsId(1, 21)).willReturn(Optional.of(student));
        given(this.studentRepository.save(student)).willReturn(student);
        given(this.clock.instant()).willReturn(Instant.parse("2026-11-04T18:00:00Z"));
        given(this.clock.getZone()).willReturn(ZoneId.of("America/Chicago"));

        assertThat(this.studentService.deactivateStudent(1).isEnabled()).isFalse();
    }

    @Test
    void testAlreadyInactiveStudentConflictsWithoutDuplicateEvent() {
        Student student = this.students.get(0);
        student.setId(1);
        student.setEnabled(false);
        given(this.userUtils.getUserId()).willReturn(20);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(true);
        given(this.studentRepository.findByIdAndSectionCourseCourseAdminId(1, 20)).willReturn(Optional.of(student));

        assertThatThrownBy(() -> this.studentService.deactivateStudent(1))
                .isInstanceOf(StudentAlreadyDeactivatedException.class);
        assertThat(student.isEnabled()).isFalse();
        verify(this.studentRepository, never()).save(any());
        verify(this.statusChangeRepository, never()).save(any());
    }

    @Test
    void testDeleteStudent() {
        // Given
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        doNothing().when(this.studentRepository).deleteById(1);

        // When
        this.studentService.deleteStudent(1);

        // Then
        verify(this.studentRepository, times(1)).deleteById(1);
    }

}
