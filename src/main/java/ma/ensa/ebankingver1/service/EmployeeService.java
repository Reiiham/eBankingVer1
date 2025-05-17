package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.EmployeeDTO;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
/*
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SMSService smsService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User createEmployee(EmployeeDTO employeeDTO) {
        // Check if user already exists by email
        String email = employeeDTO.getFirstName().toLowerCase() + "." + employeeDTO.getLastName().toLowerCase() + "@ebanking.com";
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(email);
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setRole(Role.EMPLOYEE);

        String token = generateToken(); // plain password/token to send by SMS

        employee.setToken(token);  // store token only if needed, else remove this line

        // Hash the token before saving as password
        String hashedPassword = passwordEncoder.encode(token);
        employee.setPassword(hashedPassword);

        employee.setMustChangePassword(true);

        employee = userRepository.save(employee);

        // Send SMS with the plain token
        String message = String.format("Bienvenue chez eBanking ! Votre ID est : %d. Utilisez ce token pour votre première connexion : %s", employee.getId(), token);
        smsService.sendSMS(employeeDTO.getPhoneNumber(), message);

        return employee;
    }

    // ... changePassword() unchanged, but you should also hash newPassword here
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (!user.getMustChangePassword()) {
            throw new IllegalStateException("Le changement de mot de passe n'est pas requis");
        }

        // Hash the new password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setMustChangePassword(false);

        userRepository.save(user);
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }
}

 */
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // injecte un PasswordEncoder
    @Autowired
    private SMSService smsService;  // injecter le service SMS

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    //sans sms

    public User createEmployee(EmployeeDTO employeeDTO) {
        String email = employeeDTO.getFirstName().toLowerCase() + "." + employeeDTO.getLastName().toLowerCase() + "@ebanking.com";

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(email);
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setRole(Role.EMPLOYEE);

        // Générer mot de passe temporaire
        String tempPassword = generateRandomPassword();

        // Hasher mot de passe
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employee.setMustChangePassword(true);

        employee = userRepository.save(employee);

        // Pour l'instant, on ne fait pas l’envoi SMS — on pourra afficher le mdp temporaire dans la console ou dans la réponse.

        System.out.println("Mot de passe temporaire (à communiquer à l'employé) : " + tempPassword);

        return employee;
    }



    //sms
    /*
    public User createEmployee(EmployeeDTO employeeDTO) {
        String email = employeeDTO.getFirstName().toLowerCase() + "." + employeeDTO.getLastName().toLowerCase() + "@ebanking.com";

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(email);
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setRole(Role.EMPLOYEE);

        String tempPassword = generateRandomPassword();
        employee.setPassword(passwordEncoder.encode(tempPassword));
        employee.setMustChangePassword(true);

        employee = userRepository.save(employee);

        String message = String.format("Bienvenue chez eBanking ! Votre identifiant est %s. Votre mot de passe temporaire est : %s. Merci de le changer à la première connexion.", email, tempPassword);

        try {
            smsService.sendSMS(employeeDTO.getPhoneNumber(), message);
        } catch (Exception e) {
            // Log erreur mais ne pas bloquer la création de l'employé
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
            e.printStackTrace();
        }

        return employee;
    }

     */


    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (!user.getMustChangePassword()) {
            throw new IllegalStateException("Le changement de mot de passe n'est pas requis");
        }

        // Hash the new password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setMustChangePassword(false);

        userRepository.save(user);
    }
}
