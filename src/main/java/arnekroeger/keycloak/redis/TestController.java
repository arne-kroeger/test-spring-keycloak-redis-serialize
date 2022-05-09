package arnekroeger.keycloak.redis;

import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

  @GetMapping("/hello")
  @ResponseBody
  public String hello(Authentication auth) {
    return "Roles: " +((SimpleKeycloakAccount)auth.getDetails()).getRoles().toString();
  }

}
