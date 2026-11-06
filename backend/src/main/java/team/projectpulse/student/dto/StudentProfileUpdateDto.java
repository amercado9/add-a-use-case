package team.projectpulse.student.dto;

import jakarta.validation.constraints.NotEmpty;

public record StudentProfileUpdateDto(
        @NotEmpty(message = "username is required.") String username,
        @NotEmpty(message = "first name is required.") String firstName,
        @NotEmpty(message = "last name is required.") String lastName,
        @NotEmpty(message = "email is required.") String email
) {
}
