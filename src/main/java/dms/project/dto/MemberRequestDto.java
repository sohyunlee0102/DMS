package dms.project.dto;

import dms.project.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class MemberRequestDto {

    @Getter
    @Setter
    public static class JoinDto {

        @NotBlank
        String password;

        @NotBlank
        String name;

        @NotBlank
        @Email
        String email;

        @NotNull
        Role role;
    }
}
