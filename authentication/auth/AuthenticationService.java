package za.co.nharire.order_ms.authentication.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.authentication.config.JwtService;
import za.co.nharire.order_ms.authentication.role.AuthenticationResponse;
//import za.co.nharire.order_ms.authentication.role.RoleRepository;
import za.co.nharire.order_ms.authentication.user.User;
import za.co.nharire.order_ms.authentication.user.UserRepository;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthenticationService {

//    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public void register(RegistrationRequest request) {
//        var userRole = roleRepository.findByName("USER")
//                .orElseThrow(() -> new IllegalStateException(("ROLE USER was not initialised")));
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest){
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        var claims = new HashMap<String,Object>();
        var user = ((User)auth.getPrincipal());
        claims.put("fullName",user.fullName());
        var jwtToken = jwtService.generateToken(claims,user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
