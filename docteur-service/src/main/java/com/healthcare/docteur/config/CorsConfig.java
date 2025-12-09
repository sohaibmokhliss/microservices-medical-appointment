package com.healthcare.docteur.config;

import com.healthcare.docteur.entities.Docteur;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class CorsConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Expose entity IDs
        config.exposeIdsFor(Docteur.class);

        // CORS is handled by API Gateway - disabled here to prevent conflicts
        // cors.addMapping("/**")
        //         .allowedOrigins("http://localhost:3000")
        //         .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        //         .allowedHeaders("*")
        //         .allowCredentials(true);
    }

    // CORS is handled by API Gateway - this filter is disabled to prevent conflicts
    // @Bean
    // public CorsFilter corsFilter() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowCredentials(true);
    //     config.addAllowedOrigin("http://localhost:3000");
    //     config.addAllowedHeader("*");
    //     config.addAllowedMethod("*");
    //     source.registerCorsConfiguration("/**", config);
    //     return new CorsFilter(source);
    // }
}
