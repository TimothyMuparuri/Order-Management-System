package za.co.nharire.order_ms.authentication.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.nharire.order_ms.authentication.user.User;

@Data
@Builder
@AllArgsConstructor

public class AuthenticationResponse {

    private String token;
    private User user;
}
