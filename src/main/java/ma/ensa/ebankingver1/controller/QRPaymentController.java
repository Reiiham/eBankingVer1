package ma.ensa.ebankingver1.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import ma.ensa.ebankingver1.DTO.ApiResponse;
import ma.ensa.ebankingver1.DTO.QRPaymentRequest;
import ma.ensa.ebankingver1.service.BankAccountService;
import ma.ensa.ebankingver1.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/qr-payments")
public class QRPaymentController {

    private static final Logger log = LoggerFactory.getLogger(QRPaymentController.class);

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateQRCode(@Valid @RequestBody QRPaymentRequest request, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            log.error("Validation errors in generateQRCode: {}", errors);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, errors));
        }
        String qrCodeImage = bankAccountService.generateQRPaymentCode(request);
        return ResponseEntity.ok(new ApiResponse<>(true, qrCodeImage, null));
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<String>> processQRPayment(
            @Valid @RequestBody QRPaymentRequest request,
            BindingResult result,
            Authentication authentication) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            log.error("Validation errors in processQRPayment: {}", errors);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, errors));
        }
        log.info("Received QR payment request: {}", request);
        try {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);
            log.info("Processing QR payment for username={}, userId={}", username, userId);
            bankAccountService.processQRPayment(request, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, null));
        } catch (Exception e) {
            log.error("Error processing QR payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/test-account/{id}")
    public ResponseEntity<String> testAccount(@PathVariable String id) {
        bankAccountService.testFindById(id);
        return ResponseEntity.ok("Account found: " + id);
    }
}