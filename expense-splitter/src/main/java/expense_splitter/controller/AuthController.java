package expense_splitter.controller;

import expense_splitter.dto.AuthResponse;
import expense_splitter.dto.LoginRequest;
import expense_splitter.dto.RegisterRequest;
import expense_splitter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// this class handles HTTP requests
// return values automatically converted to JSON

@RequestMapping("/api/auth")
// every endpoint in this controller starts with /api/auth
public class AuthController {

    private final AuthService authService;
    // controller only talks to service
    // never directly to repository

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    // full path = /api/auth/register
    // handles POST requests only
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        // @RequestBody → read JSON from request body
        // @Valid → trigger validation on RegisterRequest fields
        //          if name/email/password invalid →
        //          GlobalExceptionHandler returns 400 automatically

        return ResponseEntity.ok(authService.register(request));
        // ResponseEntity.ok() = HTTP 200 status
        // authService.register() does all the work
        // controller just passes and returns
    }

    @PostMapping("/login")
    // full path = /api/auth/login
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}