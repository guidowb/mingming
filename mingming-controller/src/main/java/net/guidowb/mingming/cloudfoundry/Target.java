package net.guidowb.mingming.cloudfoundry;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Entity
public class Target {

	private @Id @GeneratedValue Long id;
	private URL target;
	private String refreshToken;

	private @Transient CloudFoundryClient client = null;

	private Target(URL target, CloudFoundryClient client, String refreshToken) {
		this.target = target;
		this.client = client;
		this.refreshToken = refreshToken;
	}

	public CloudFoundryClient getClient() {
		if (client == null) {
			OAuth2AccessToken newToken = new DefaultOAuth2AccessToken("invalid-token");
			((DefaultOAuth2AccessToken) newToken).setRefreshToken(new DefaultOAuth2RefreshToken(refreshToken));
			CloudCredentials newCredentials = new CloudCredentials(newToken);
			client = new CloudFoundryClient(newCredentials, target);
			OAuth2AccessToken accessToken = client.login();
			refreshToken = accessToken.getRefreshToken().getValue();
		}
		return client;
	}

	public static Target createTarget(String username, String password, String url) throws MalformedURLException {
		URL targetURL = URI.create(url).toURL();
		CloudCredentials credentials = new CloudCredentials(username, password);
		CloudFoundryClient client = new CloudFoundryClient(credentials, targetURL);
		OAuth2AccessToken accessToken = client.login();
		String refreshToken = accessToken.getRefreshToken().getValue();
		return new Target(targetURL, client, refreshToken);
	}
}
