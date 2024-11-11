package cn.guet.ksmvcmain.config;

import cn.guet.ksmvcmain.exception.handle.AuthenticationHandle;
import cn.guet.ksmvcmain.exception.handle.NotAuthHandle;
import cn.guet.ksmvcmain.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    private AuthenticationHandle authenticationHandle;
    @Autowired
    private NotAuthHandle notAuthHandle;

    @Bean
    public BCryptPasswordEncoder getPw() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(v -> {
            try {
                v.disable().sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/swagger/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/v3/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/doc.html").permitAll()
                .requestMatchers("/user/register").anonymous()
                .requestMatchers("/user/login").anonymous()
                .requestMatchers("/articles/all").permitAll()
                .requestMatchers("/articles/id").permitAll()
                .requestMatchers("/articles/ids/**").permitAll()
                .requestMatchers("/articles/comment").permitAll()
                .requestMatchers("/articles/recommend/all").permitAll()
                .requestMatchers("/articles/add").permitAll()
                .requestMatchers("/tag/all").permitAll()
                .requestMatchers("/search/**").permitAll()
                .requestMatchers("/endpoint").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/app/**").permitAll()
                .requestMatchers("/topic/**").permitAll()
                .requestMatchers("/question/list").permitAll()
                .requestMatchers("/question/code").permitAll()
                .anyRequest().authenticated());
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationHandle)
                .accessDeniedHandler(notAuthHandle)
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
