package expense_splitter.security;

// Spring Security imports
import expense_splitter.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // marks this as a Spring managed service bean
public class UserDetailsServiceImpl implements UserDetailsService {
    // implements UserDetailsService — Spring Security's interface
    // forces us to implement loadUserByUsername method

    private final UserRepository userRepository;
    // final — means this dependency will never change after injection
    // we use UserRepository to find user in DB

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        // constructor injection — Spring automatically passes
        // UserRepository when creating this class
        // better than @Autowired — makes dependencies explicit
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        // Spring Security calls this method automatically
        // whenever it needs to load a user
        // parameter is called "username" in the interface
        // but we use email as our username

        expense_splitter.entity.User user = userRepository
                .findByEmail(email) // find user by email in DB
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                        // if user doesn't exist throw this exception
                        // Spring Security handles this exception automatically
                ));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // set email as username
                .password(user.getPassword()) // set hashed password
                .authorities("ROLE_USER") // set default role for all users
                .build(); // build and return UserDetails object
    }
}