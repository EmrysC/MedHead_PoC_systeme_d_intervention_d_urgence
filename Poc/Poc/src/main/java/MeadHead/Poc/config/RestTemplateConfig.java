package MeadHead.Poc.config; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Configure les clients HTTP nécessaires pour les appels API externes.
 */
@Configuration 
public class RestTemplateConfig {

    @Bean 
    public RestTemplate restTemplate() {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); 

        restTemplate.getMessageConverters().clear();
        
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        

        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());


        return restTemplate;
    }
}