package za.co.nharire.order_ms.authentication.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthenticationRequest {

    @Email(message = "Email is not formatted")
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is mandatory")
    @Size(min= 4,message = "password should be at least 4 characters")
    private String password;
}
