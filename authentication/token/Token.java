//package za.co.nharire.order_ms.authentication.token;
//
//import jakarta.persistence.*;
//import lombok.Builder;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import za.co.nharire.order_ms.authentication.user.User;
//
//import java.time.LocalDateTime;
//
//@Data
//@Builder
//@RequiredArgsConstructor
//@Entity
//public class Token {
//
//    @Id
//    @GeneratedValue
//    private int id;
//    private String token;
//    private LocalDateTime createdAt;
//    private LocalDateTime expiredAt;
//    private LocalDateTime validatedAt;
//
//    @ManyToOne
//    @JoinColumn(name = "userId", nullable = false)
//    private User user;
//}