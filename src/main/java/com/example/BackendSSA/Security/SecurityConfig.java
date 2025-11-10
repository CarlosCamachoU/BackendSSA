package com.example.BackendSSA.Security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    //verificar información de usuarios al loguearse
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //encriptar contraseñas

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // filtro de seguridad de jwt creado en la clase anterior

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }


    //establecer cadenas de filtro de seguridad para permisos según rol
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 2. Lógica CORS 
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("*")); // Permite cualquier origen
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(
                                            Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
                    return config;
                }))
                
                // 3. Deshabilita CSRF
                .csrf(csrf -> csrf.disable())
                
                // 4. Configura el manejo de excepciones con el EntryPoint
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                
                // 5. Configura la gestión de sesiones como SIN ESTADO (STATELESS)
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 6. Define las reglas de autorización para las solicitudes
                .authorizeHttpRequests((authorize) -> authorize
                        
                        // Permite acceso libre a login 
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        // Permite acceso libre a registro 
                        .requestMatchers("/api/auth/registro").permitAll() 

                        // 🟢 NUEVAS RUTAS PÚBLICAS PARA EL RESTABLECIMIENTO DE CONTRASEÑA 
                        .requestMatchers("/api/auth/forgot-password").permitAll() // Solicitud de token
                        .requestMatchers("/api/auth/reset-password").permitAll()  // Envío de nueva contraseña

                        // Permite acceso al catálogo y a las categorías
                        .requestMatchers("/api/categorias/categorias/**").permitAll() // 🛑 Nueva ruta simplificada
                        .requestMatchers("/api/productos").permitAll()
                        .requestMatchers("/api/productos/**").permitAll()

                        
                        // Rutas de administración d
                        .requestMatchers(
                                "/api/Administrador/",
                                "/api/Usuario/"
                                //"/api/categorias/" // Ejemplo de ruta protegida para el Admin/TI
                                ).hasAnyAuthority("Administrador", "Usuario") // Ajusta según los roles que tengas
                        
                        // Las demás rutas requieren un usuario autenticado (incluye /api/productos, etc.)
                        .anyRequest().authenticated());
        
        // 7. INTEGRA EL FILTRO JWT en la cadena de seguridad
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }



    
}
