package expense_splitter.service;

import expense_splitter.dto.AuthResponse;
import expense_splitter.dto.LoginRequest;
import expense_splitter.dto.RegisterRequest;
import expense_splitter.entity.User;
import expense_splitter.repository.UserRepository;
import expense_splitter.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {

        // STEP 1 — is this email already taken?
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
            // isPresent() checks if Optional has a value
            // if email exists → throw error → GlobalExceptionHandler
            // returns 400 to client
        }

        // STEP 2 — build the User object
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // encode() hashes plain password using BCrypt
                // "secret123" → "$2a$10$xK9Lm..."
                // NEVER store plain text passwords
                .build();

        // STEP 3 — save to database
        userRepository.save(user);
        // Hibernate runs INSERT INTO users(...) automatically

        // STEP 4 — generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        // creates token with email baked inside
        // expires in 24 hours

        // STEP 5 — return token to client
        return new AuthResponse(token);
        // AuthResponse just wraps the token string
        // client receives { "token": "eyJhbGci..." }
    }

    public AuthResponse login(LoginRequest request) {

        // STEP 1 — verify credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // AuthenticationManager does this internally:
        // 1. calls loadUserByUsername(email) → loads user from DB
        // 2. BCrypt compares plain password with stored hash
        // 3. match ✅ → continues
        // 4. no match ❌ → throws BadCredentialsException → 400

        // STEP 2 — get user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        // we know user exists because step 1 passed
        // but we need User object to get email for token

        // STEP 3 — generate token
        String token = jwtUtil.generateToken(user.getEmail());

        // STEP 4 — return token
        return new AuthResponse(token);
    }
}