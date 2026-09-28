package com.project.contactsdemo.core.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.project.contactsdemo.core.properties.RateLimitProperties;
import java.util.Map;
//Yani tekrardan hatırlamak adına
//Burası soft-coding yapacağımız kısmın Obje tabanlı bri dilde
// 'class' olarak represent edilmesini gösteriyor. Aslında belki de
/* config dediğimiz şey budur! Biraz bakalım bu kavrama*/
// sonuç :Now, we can inject this RateLimiterConfig anywhere in the app.
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
@Setter
public class RateLimiterConfig {
    private Boolean enabled;
    private Map<String, RateLimitProperties> limits;

    public Boolean getEnabled() {
        return enabled;
    }

    public Map<String, RateLimitProperties> getLimits() {
        return limits;
    }
}
