package expense_splitter.security;
// this file belongs to the security package

import expense_splitter.security.JwtUtil;
import expense_splitter.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
// Spring manages this as a bean
// @Component because it's not a @Service or @Repository
public class JwtFilter extends OncePerRequestFilter {
// extends OncePerRequestFilter — guarantees this filter
// runs exactly once per request, never twice

    private final JwtUtil jwtUtil;
    // we need JwtUtil to validate token and extract email

    private final UserDetailsServiceImpl userDetailsService;
    // we need this to load user from DB by email

    public JwtFilter(JwtUtil jwtUtil,
                     UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        // constructor injection — Spring automatically provides both
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,    // the incoming HTTP request
            HttpServletResponse response,  // the outgoing HTTP response
            FilterChain filterChain)       // the chain of filters
            throws ServletException, IOException {
        // this method runs on EVERY request automatically
        // Spring calls this — we never call it manually

        // Step 1 — read the Authorization header from request
        String authHeader = request.getHeader("Authorization");
        // Authorization header looks like:
        // "Bearer eyJhbGciOiJIUzI1NiJ9..."
        // if no token sent → authHeader = null

        String token = null;
        // will hold the extracted JWT token
        String email = null;
        // will hold the email extracted from token

        // Step 2 — extract token from header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // check header exists AND starts with "Bearer "
            token = authHeader.substring(7);
            // substring(7) removes first 7 characters "Bearer "
            // leaving just the raw token string
            email = jwtUtil.extractEmail(token);
            // extract email from inside the token using JwtUtil
        }

        // Step 3 — validate token and authenticate user
        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            // email != null → we successfully extracted email from token
            // authentication == null → user not yet authenticated
            // in this request (prevents double authentication)

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);
            // load full user details from DB using email
            // calls UserDetailsServiceImpl.loadUserByUsername()

            if (jwtUtil.isTokenValid(token)) {
                // double check — is token genuine and not expired?

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // who is authenticated
                                null,                           // credentials — null because JWT handles this
                                userDetails.getAuthorities()    // their roles e.g. ROLE_USER
                        );
                // creates an authentication object for Spring Security

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );
                // attaches extra request details to authentication object
                // like IP address, session id etc.

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
                // THIS IS THE KEY LINE
                // tells Spring Security — this request belongs to this user
                // Spring Security reads this for every request
                // cleared automatically after request ends
            }
        }

        // Step 4 — continue to next filter or controller
        filterChain.doFilter(request, response);
        // if authentication was set → request continues to controller
        // if authentication was NOT set → Spring Security
        // automatically returns 401 Unauthorized
        // request never reaches controller
    }
}