package ma.ensa.ebankingver1.controller;

import jakarta.validation.*;
import ma.ensa.ebankingver1.DTO.ResendActivationRequest;
import ma.ensa.ebankingver1.DTO.SetPasswordRequest;
import ma.ensa.ebankingver1.service.AuditService;
import ma.ensa.ebankingver1.service.EmployeeService;
import ma.ensa.ebankingver1.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class PasswordController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EnrollmentService enrollmentService; // Ajout du service client

    @Autowired
    private AuditService auditService;

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    // Endpoint pour vérifier la validité d'un token - VERSION DEBUG
    @GetMapping("/validate-token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable("token") String token) {
        logger.info("=== VALIDATE TOKEN START ===");
        logger.info("Received token: {}", token);
        logger.info("Token length: {}", token != null ? token.length() : "null");

        try {
            // Vérification de base
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token is null or empty");
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Token vide"
                ));
            }

            logger.info("Calling token validation services...");

            // Essayer d'abord avec le service employé
            boolean isValidEmployee = employeeService.isTokenValid(token);
            if (isValidEmployee) {
                logger.info("Token is valid for employee - returning success");
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token valide",
                        "type", "employee"
                ));
            }

            // Si pas valide pour employé, essayer avec le service client
            boolean isValidClient = enrollmentService.isTokenValid(token);
            if (isValidClient) {
                logger.info("Token is valid for client - returning success");
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token valide",
                        "type", "client"
                ));
            }

            logger.warn("Token is invalid or expired for both services - returning failure");
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Token invalide ou expiré"
            ));

        } catch (Exception e) {
            logger.error("=== EXCEPTION IN VALIDATE TOKEN ===");
            logger.error("Exception type: {}", e.getClass().getSimpleName());
            logger.error("Exception message: {}", e.getMessage());
            logger.error("Stack trace: ", e);

            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Erreur lors de la validation du token: " + e.getMessage()
            ));
        } finally {
            logger.info("=== VALIDATE TOKEN END ===");
        }
    }

    // Endpoint pour définir le mot de passe avec le token
    @PostMapping("/set-password")
    public ResponseEntity<?> setPasswordWithToken(@Valid @RequestBody SetPasswordRequest request) {
        try {
            logger.info("Setting password with token: {}", request.getToken().substring(0, 8) + "...");

            // Essayer d'abord avec le service employé
            try {
                if (employeeService.isTokenValid(request.getToken())) {
                    employeeService.setPasswordWithToken(request.getToken(), request.getNewPassword());
                    auditService.logAction("SET_PASSWORD_SUCCESS", "EMPLOYEE", null,
                            Map.of("token_prefix", request.getToken().substring(0, 8)), true);
                    return ResponseEntity.ok(Map.of("message", "Mot de passe défini avec succès"));
                }
            } catch (IllegalArgumentException e) {
                // Continue vers le service client
                logger.debug("Token not valid for employee service, trying client service");
            }

            // Essayer avec le service client
            try {
                if (enrollmentService.isTokenValid(request.getToken())) {
                    enrollmentService.setPasswordWithToken(request.getToken(), request.getNewPassword());
                    auditService.logAction("SET_PASSWORD_SUCCESS", "CLIENT", null,
                            Map.of("token_prefix", request.getToken().substring(0, 8)), true);
                    return ResponseEntity.ok(Map.of("message", "Mot de passe défini avec succès"));
                }
            } catch (IllegalArgumentException e) {
                // Token invalide pour les deux services
                logger.error("Token invalid for both services: {}", e.getMessage());
            }

            // Si on arrive ici, le token n'est valide pour aucun service
            auditService.logAction("SET_PASSWORD_FAILED", "USER", null,
                    Map.of("error", "Token invalide"), false);
            return ResponseEntity.badRequest().body(Map.of("error", "Token invalide ou expiré"));

        } catch (Exception e) {
            logger.error("Internal error setting password: {}", e.getMessage());
            auditService.logAction("SET_PASSWORD_FAILED", "USER", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne"));
        }
    }

    // Endpoint pour renvoyer un lien d'activation (employé)
    @PostMapping("/resend-activation")
    public ResponseEntity<?> resendActivationLink(@Valid @RequestBody ResendActivationRequest request) {
        try {
            logger.info("Resending activation link to: {}", request.getEmail());
            employeeService.resendActivationLink(request.getEmail());
            auditService.logAction("RESEND_ACTIVATION", "EMPLOYEE", null,
                    Map.of("email", request.getEmail()), true);
            return ResponseEntity.ok(Map.of("message", "Lien d'activation renvoyé"));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to resend activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "EMPLOYEE", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal error resending activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "EMPLOYEE", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne"));
        }
    }

    // Endpoint pour renvoyer un lien d'activation (client)
    @PostMapping("/resend-activation-client")
    public ResponseEntity<?> resendActivationLinkClient(@Valid @RequestBody ResendActivationRequest request) {
        try {
            logger.info("Resending client activation link to: {}", request.getEmail());
            enrollmentService.resendActivationLink(request.getEmail());
            auditService.logAction("RESEND_ACTIVATION", "CLIENT", null,
                    Map.of("email", request.getEmail()), true);
            return ResponseEntity.ok(Map.of("message", "Lien d'activation client renvoyé"));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to resend client activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "CLIENT", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal error resending client activation: {}", e.getMessage());
            auditService.logAction("RESEND_ACTIVATION_FAILED", "CLIENT", null,
                    Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne"));
        }
    }
}