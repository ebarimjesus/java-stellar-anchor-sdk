package org.stellar.anchor.platform.config;

import static org.stellar.anchor.util.Log.debugF;
import static org.stellar.anchor.util.StringHelper.isEmpty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.stellar.anchor.api.exception.SepException;
import org.stellar.anchor.sep10.Sep10Helper;

@Data
public class ClientsConfig implements Validator {
  List<ClientConfig> clients = Lists.newLinkedList();
  Map<String, ClientConfig> clientMap = null;
  Map<String, String> domainToClientNameMap = null;
  Map<String, String> signingKeyToClientNameMap = null;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ClientConfig {
    String name;
    ClientType type;
    String signingKey;
    String domain;
    String callbackUrl;
  }

  public enum ClientType {
    CUSTODIAL,
    NONCUSTODIAL
  }

  public ClientConfig getClientConfigBySigningKey(String signingKey) {
    if (signingKeyToClientNameMap == null) {
      signingKeyToClientNameMap = Maps.newHashMap();
      clients.forEach(
          clientConfig -> {
            if (clientConfig.signingKey != null) {
              signingKeyToClientNameMap.put(clientConfig.signingKey, clientConfig.name);
            }
          });
    }
    return getClientConfigByName(signingKeyToClientNameMap.get(signingKey));
  }

  public ClientConfig getClientConfigByDomain(String domain) {
    if (domainToClientNameMap == null) {
      domainToClientNameMap = Maps.newHashMap();
      clients.forEach(
          clientConfig -> {
            if (clientConfig.domain != null) {
              domainToClientNameMap.put(clientConfig.domain, clientConfig.name);
            }
          });
    }
    return getClientConfigByName(domainToClientNameMap.get(domain));
  }

  public ClientConfig getClientConfigByName(String name) {
    if (clientMap == null) {
      clientMap = Maps.newHashMap();
      clients.forEach(clientConfig -> clientMap.put(clientConfig.name, clientConfig));
    }
    return clientMap.get(name);
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return ClientsConfig.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ClientsConfig configs = (ClientsConfig) target;
    configs.clients.forEach(clientConfig -> validateClient(clientConfig, errors));
  }

  private void validateClient(ClientConfig clientConfig, Errors errors) {
    debugF("Validating client {}", clientConfig);
    if (isEmpty(clientConfig.name)) {
      errors.reject("empty-client-name", "The client.name cannot be empty and must be defined");
    }
    if (clientConfig.type.equals(ClientType.CUSTODIAL)) {
      validateCustodialClient(clientConfig, errors);
    } else {
      validateNonCustodialClient(clientConfig, errors);
    }
  }

  void validateCustodialClient(ClientConfig clientConfig, Errors errors) {
    if (isEmpty(clientConfig.signingKey)) {
      errors.reject(
          "empty-client-signing-key", "The client.signingKey cannot be empty and must be defined");
    }
    if (!isEmpty(clientConfig.callbackUrl)) {
      try {
        new URL(clientConfig.callbackUrl);
      } catch (MalformedURLException e) {
        errors.reject("client-invalid-callback_url", "The client.callbackUrl is invalid");
      }
    }
  }

  void validateNonCustodialClient(ClientConfig clientConfig, Errors errors) {
    if (isEmpty(clientConfig.domain)) {
      errors.reject("empty-client-domain", "The client.domain cannot be empty and must be defined");
    }

    if (!isEmpty(clientConfig.signingKey)) {
      try {
        String clientSigningKey = Sep10Helper.fetchSigningKeyFromClientDomain(clientConfig.domain);
        if (!clientConfig.signingKey.equals(clientSigningKey)) {
          errors.reject(
              "client-signing-key-does-not-match",
              "The client.signingKey does not matched any valid registered keys");
        }
      } catch (SepException e) {
        errors.reject(
            "client-signing-key-toml-read-failure",
            "SIGNING_KEY not present in 'client_domain' TOML or TOML file does not exist");
      }
    }

    if (!isEmpty(clientConfig.callbackUrl)) {
      try {
        new URL(clientConfig.callbackUrl);
      } catch (MalformedURLException e) {
        errors.reject("client-invalid-callback_url", "The client.callbackUrl is invalid");
      }
    }
  }
}
