package za.co.nharire.order_ms.authentication.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@Entity
@Table(name = "_user")

public class User {

    @Id
    private Integer userId;
    private String email;
    private String password;
    private String role;
}
