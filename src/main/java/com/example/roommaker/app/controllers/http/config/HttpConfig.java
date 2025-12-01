package com.example.roommaker.app.controllers.http.config;



import com.example.roommaker.app.controllers.http.filters.JwtHTTPInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class HttpConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    private final JwtHTTPInterceptor jwtHTTPInterceptor;

    public HttpConfig(JwtHTTPInterceptor jwtHTTPInterceptor) {
        this.jwtHTTPInterceptor = jwtHTTPInterceptor;
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite todas as rotas
                 .allowedOrigins(allowedOrigins)
                .allowedMethods("*") // Permite todos os métodos (GET, POST, PUT, DELETE, etc.)
                .allowedHeaders("*") // Permite todos os cabeçalhos
                .allowCredentials(true);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtHTTPInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/cadastro",
                        "/login2fa",
                        "/usuario/esquecisenha",
                        "/usuario/novasenha",
                        "/swagger-ui/",
                        "/ping"
                );
    }

}
