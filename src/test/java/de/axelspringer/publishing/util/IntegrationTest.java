package de.axelspringer.publishing.util;

import com.jayway.restassured.RestAssured;
import de.axelspringer.publishing.Application;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@org.springframework.boot.test.IntegrationTest({"server.port:0"})
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public abstract class IntegrationTest {
    @Value("${local.server.port}")
    int port;

    @Autowired
    SecurityProperties securityProperties;

    @Before
    public void initRestAssured() {
        RestAssured.port = port;
        SecurityProperties.Basic basicAuth = securityProperties.getBasic();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        if(basicAuth.isEnabled()) {
            RestAssured.authentication = RestAssured.basic(securityProperties.getUser().getName(), securityProperties.getUser().getPassword());
        }
    }
}
