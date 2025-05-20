package ma.ensa.ebankingver1.controller;

import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.service.UserService;
import ma.ensa.ebankingver1.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        try {
            logger.info("Login attempt for email: {}", email);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            String token = jwtUtil.generateToken(email);
            Role role = userService.getUserRoleByEmail(email);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role.name());
            response.put("message", "Authentification réussie !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for email: {}, error: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
        }
    }
    @GetMapping("/role")
    public ResponseEntity<?> getRole(Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.info("Fetching role for email: {}", email);
            Role role = userService.getUserRoleByEmail(email);
            Map<String, String> response = new HashMap<>();
            response.put("role", role.name());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Role not found for email: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found");
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching role");
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "Welcome Admin";
    }

    @GetMapping("/client/dashboard")
    public String clientDashboard() {
        return "Welcome Client";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "Welcome Employee";
    }
}
/*
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        //        String email = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            String token = jwtUtil.generateToken(email);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Authentification réussie !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
        }
    }
    @GetMapping("/role")
    public ResponseEntity<?> getRole(Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.info("Fetching role for email: {}", email);
            Role role = userService.getUserRoleByEmail(email);
            Map<String, String> response = new HashMap<>();
            response.put("role", role.name());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Role not found for email: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found");
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching role");
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "Welcome Admin";
    }

    @GetMapping("/client/dashboard")
    public String clientDashboard() {
        return "Welcome Client";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "Welcome Employee";
    }
}


 */



