package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.DTO.EmployeeDTO;
import ma.ensa.ebankingver1.model.Role;
import ma.ensa.ebankingver1.model.User;
import ma.ensa.ebankingver1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SMSService smsService;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User createEmployee(EmployeeDTO employeeDTO) {
        //verifier si employee existe
        if (userRepository.findByEmail(employeeDTO.getFirstName() + "." + employeeDTO.getLastName() + "@ebanking.com").isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User employee = new User();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getFirstName() + "." + employeeDTO.getLastName() + "@ebanking.com");
        employee.setTel(employeeDTO.getPhoneNumber());
        employee.setRole(Role.EMPLOYEE);
        String token = generateToken();
        employee.setToken(token);
        employee.setPassword(token);
        employee.setMustChangePassword(true);

        employee = userRepository.save(employee);

        String message = String.format("Bienvenue chez eBanking ! Votre ID est : %d. Utilisez ce token pour votre première connexion : %s", employee.getId(), token);
        smsService.sendSMS(employeeDTO.getPhoneNumber(), message);

        return employee;
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (!user.getMustChangePassword()) {
            throw new IllegalStateException("Le changement de mot de passe n'est pas requis");
        }

        user.setPassword(newPassword);
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