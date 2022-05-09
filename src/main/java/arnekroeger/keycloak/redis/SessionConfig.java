package arnekroeger.keycloak.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;

@Configuration
public class SessionConfig implements BeanClassLoaderAware  {

  private ClassLoader loader;

  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
    return new GenericJackson2JsonRedisSerializer(objectMapper());
  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(SecurityJackson2Modules.getModules(this.loader));
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.addMixIn(KeycloakAuthenticationToken.class, KeycloakAuthenticationTokenMixin.class);
    mapper.addMixIn(SimpleKeycloakAccount.class, SimpleKeycloakAccountMixin.class);
    mapper.addMixIn(KeycloakPrincipal.class, KeycloakPrincipalMixin.class);
    mapper.addMixIn(RefreshableKeycloakSecurityContext.class, RefreshableKeycloakSecurityContextMixin.class);
    mapper.addMixIn(AccessToken.class, AccessToken.class);
    mapper.addMixIn(AccessToken.Access.class, AccessToken.Access.class);
    mapper.addMixIn(IDToken.class, IDToken.class);
    mapper.addMixIn(HashSet.class, HashSet.class);

    return mapper;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.loader = classLoader;
  }

  private abstract static class KeycloakAuthenticationTokenMixin {
    @JsonCreator
    public KeycloakAuthenticationTokenMixin(@JsonProperty("details") KeycloakAccount account,
                             @JsonProperty("interactive") boolean interactive) {}

    @JsonIgnore
    public abstract Object getCredentials();

    @JsonIgnore
    public abstract OidcKeycloakAccount getAccount();

    @JsonIgnore
    public abstract String getName();
  }

  private abstract static class SimpleKeycloakAccountMixin {
    @JsonCreator
    public SimpleKeycloakAccountMixin(@JsonProperty("principal") Principal principal, @JsonProperty("roles") @JsonDeserialize(as = HashSet.class) Set<String> roles, @JsonProperty("securityContext") RefreshableKeycloakSecurityContext securityContext) {}

    @JsonIgnore
    public abstract RefreshableKeycloakSecurityContext getKeycloakSecurityContext();

  }

  private static class KeycloakPrincipalMixin <T extends KeycloakSecurityContext> {
    @JsonCreator
    public KeycloakPrincipalMixin(@JsonProperty("name") String name, @JsonProperty("keycloakSecurityContext") T context) {}
  }



  private abstract static class RefreshableKeycloakSecurityContextMixin {
    @JsonIgnore
    public abstract boolean isActive();

    @JsonIgnore
    public abstract String getRealm();

    @JsonIgnore
    public abstract KeycloakDeployment getDeployment();

  }

}
