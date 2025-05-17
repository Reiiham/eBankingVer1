package ma.ensa.ebankingver1.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.support.*;


@EnableWebMvc
@Configuration
@ComponentScan({"ma.ensa.ebankingver1.config","ma.ensa.ebankingver1.service","ma.ensa.ebankingver1.filter","ma.ensa.ebankingver1.controller", "ma.ensa.ebankingver1"})
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //Configure comment servir les ressources statiques comme CSS, JS, images
        //registry.addResourceHandler("/resources/**").addResourceLocations("/assets/");
        //registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("/angular/");

    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource rb = new ResourceBundleMessageSource();
        rb.setBasenames(new String[] { "messages/messages", "messages/validation" });
        return rb;
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }



}

