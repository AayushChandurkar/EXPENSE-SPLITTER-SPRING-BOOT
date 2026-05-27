package expense_splitter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// tells Spring — this class contains configuration beans
// Spring reads this class at startup and applies all @Bean methods

@EnableWebSecurity
// activates Spring Security for the entire application
// without this — Spring Security does nothing
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    // our custom JWT filter — needs to be added to security chain

    private final UserDetailsServiceImpl userDetailsService;
    // needed to set up authentication provider

    public SecurityConfig(JwtFilter jwtFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        // constructor injection of both dependencies
    }

    @Bean
    // @Bean tells Spring — manage the return value of this method
    // other classes can inject this object anywhere in the app
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                // disable CSRF protection
                // CSRF is needed for browser based apps with sessions
                // we use JWT which is stateless — CSRF not needed

                .authorizeHttpRequests(auth -> auth
                                // define which endpoints need authentication

                                .requestMatchers("/api/auth/**").permitAll()
                                // /api/auth/register → public, no token needed
                                // /api/auth/login    → public, no token needed
                                // ** means any path under /api/auth/

                                .anyRequest().authenticated()
                        // every other endpoint requires a valid JWT token
                        // no token → 401 Unauthorized automatically
                )

                .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // tell Spring Security — don't create HTTP sessions
                        // we are stateless — JWT carries all user info
                        // no session stored on server ever
                )

                .authenticationProvider(authenticationProvider())
                // register our custom authentication provider
                // tells Spring how to verify user credentials during login

                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);
        // add our JwtFilter BEFORE Spring's default auth filter
        // this ensures JWT is checked first on every request

        return http.build();
        // build and return the complete security configuration
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // BCrypt is the industry standard password hashing algorithm
        // one way hash — can't reverse back to plain text
        // Spring Security uses this to hash and compare passwords
        // whenever we save a password → BCrypt hashes it
        // whenever we verify a password → BCrypt compares hashes
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        // DaoAuthenticationProvider = Database Authentication Provider
        // "Dao" = Data Access Object — it accesses DB to verify users

        provider.setUserDetailsService(userDetailsService);
        // tell provider — use OUR UserDetailsServiceImpl
        // to load users from DB during authentication

        provider.setPasswordEncoder(passwordEncoder());
        // tell provider — use BCrypt to compare passwords
        // when user logs in with plain text password
        // BCrypt compares it with stored hash automatically

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        // AuthenticationManager is the main entry point
        // for authentication in Spring Security
        // our AuthService will use this to authenticate
        // users during login
        // Spring automatically configures this based on
        // our authenticationProvider above
    }
}