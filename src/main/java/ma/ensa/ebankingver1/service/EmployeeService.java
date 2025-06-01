package ma.ensa.ebankingver1.service;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import ma.ensa.ebankingver1.DTO.EmployeeDTO;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@Transactional
public class EmployeeService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailtrapClient mailtrapClient; // Changed from JavaMailSender

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_DIGITS = 6;
    private static final int USERNAME_LETTERS = 2;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User createEmployee(EmployeeDTO employeeDTO) {
        String username = generateUniqueUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setUsername(username);
        employee.setEmail(employeeDTO.getEmailPersonnel());
        employee.setCin(employeeDTO.getCin());
        //employee.setCinPhotoFilename(employeeDTO.getCinPhotoFilename()); // Stocker le nom du fichier
        employee.setRole(Role.EMPLOYEE);

        String tempPassword = generateRandomPassword();
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employee.setMustChangePassword(true);
        employee = userRepository.save(employee);

        // Send email with Mailtrap API
        String message = String.format("""
        Bonjour %s,
        
        Votre compte a été créé avec succès.
        
        Identifiant : %s
        Mot de passe temporaire : %s
        
        Veuillez vous connecter et modifier votre mot de passe dès que possible.
        
        Cordialement,
        L'équipe RH
        """, employee.getFirstName(), username, tempPassword);

        sendEmail(employee.getEmail(), "Bienvenue sur la plateforme", message);
        return employee;
    }

    public void sendEmail(String to, String subject, String content) {
        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.com", "Équipe RH"))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(content)
                    .category("Employee Registration")
                    .build();

            System.out.println("Sending email response: " + mailtrapClient.send(mail));
            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        if (!user.getMustChangePassword()) {
            throw new IllegalStateException("Le changement de mot de passe n'est pas requis");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private String generateUniqueUsername() {
        String username;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < USERNAME_LETTERS; i++) {
                sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            for (int i = 0; i < USERNAME_DIGITS; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            username = sb.toString();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }
}
