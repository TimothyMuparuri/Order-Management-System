package za.co.nharire.order_ms.authentication.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor

public class AuthenticationResponse {

    private String token;
}
