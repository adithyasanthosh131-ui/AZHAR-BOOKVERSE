
package librarymanagement.controller;

import librarymanagement.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final StudentRepository repository;
    private final PasswordEncoder encoder;

    public LoginController(
            StudentRepository repository,
            PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        var student = repository.findByEmailIgnoreCase(
                email.trim());

        if (student.isPresent() &&
                encoder.matches(
                        password,
                        student.get().getPassword())) {

            session.setAttribute(
                    "studentId", student.get().getId());

            session.setAttribute(
                    "studentName", student.get().getName());

            return "redirect:/dashboard";
        }

        model.addAttribute(
                "error", "Invalid email or password.");

        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
