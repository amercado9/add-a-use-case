package team.projectpulse.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.system.UserUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentSecurityServiceTest {

    @Mock
    StudentRepository studentRepository;

    @Mock
    UserUtils userUtils;

    @InjectMocks
    StudentSecurityService studentSecurityService;

    @Test
    void courseAdminCanManageStudentInOwnedCourse() {
        given(this.userUtils.getUserId()).willReturn(10);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(true);
        given(this.studentRepository.existsByIdAndSectionCourseCourseAdminId(4, 10)).willReturn(true);

        assertThat(this.studentSecurityService.canManageLifecycle(4)).isTrue();
    }

    @Test
    void assignedInstructorCanManageStudentInSection() {
        given(this.userUtils.getUserId()).willReturn(11);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(false);
        given(this.userUtils.hasRole("ROLE_instructor")).willReturn(true);
        given(this.studentRepository.existsByIdAndSectionInstructorsId(4, 11)).willReturn(true);

        assertThat(this.studentSecurityService.canManageLifecycle(4)).isTrue();
    }

    @Test
    void unknownOrOutOfScopeTargetFailsClosed() {
        given(this.userUtils.getUserId()).willReturn(11);
        given(this.userUtils.hasRole("ROLE_admin")).willReturn(false);
        given(this.userUtils.hasRole("ROLE_instructor")).willReturn(true);

        assertThat(this.studentSecurityService.canManageLifecycle(999)).isFalse();
    }
}
