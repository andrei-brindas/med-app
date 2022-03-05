package tk.andrei.medicalapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tk.andrei.medicalapp.filter.CustomAuthenticationFilter;
import tk.andrei.medicalapp.filter.CustomAuthorizationFilter;
import tk.andrei.medicalapp.services.JwtService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final String MANAGER = "MANAGER";
    private static final String ADMIN = "ADMIN";

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private JwtService tokenService;

    /**
     * This section defines the user accounts which can be used for
     * authentication as well as the roles each user has.
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    /**
     * This section defines the security policy for the app.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), tokenService);
        customAuthenticationFilter.setFilterProcessesUrl("/user/login");

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.httpBasic()
                .and()
                .authorizeRequests()
                // allow register and login
                .antMatchers(HttpMethod.POST, "/user/login").permitAll()
                .antMatchers(HttpMethod.POST, "/user/register").permitAll()
                //swagger
                .antMatchers( "/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                //h2
                .antMatchers( "/","/h2-console/**").permitAll()
                // the user uris
                .antMatchers(HttpMethod.GET, "/user/find/**").hasAnyRole(MANAGER, ADMIN)
                .antMatchers(HttpMethod.POST, "/user/role/**").hasAnyRole(MANAGER, ADMIN)
                .antMatchers(HttpMethod.DELETE, "/user/delete/**").hasAnyRole(MANAGER, ADMIN)
                .antMatchers(HttpMethod.GET, "/user/getCurrentUser").hasAnyRole();

        http.authorizeRequests().anyRequest().fullyAuthenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(tokenService), UsernamePasswordAuthenticationFilter.class);

        http.csrf().disable();
        http.headers().frameOptions().disable();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
