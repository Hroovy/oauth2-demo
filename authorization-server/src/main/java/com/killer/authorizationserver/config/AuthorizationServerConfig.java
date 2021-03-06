package com.killer.authorizationserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * <p>
 *      我需要自己实现authoziation_code授权方式，用户名密码授权，隐式授权
 * </p>
 * @author killer
 * @date 2019/07/24 - 22:38
 */

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    public AuthorizationServerConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // private final AuthenticationManager authenticationManager;
    //
    // @Autowired
    // public AuthorizationServerConfig(AuthenticationManager authenticationManager) {
    //     this.authenticationManager = authenticationManager;
    // }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        super.configure(security);
        // security.authenticationEntryPoint() 用来exception 跳转
        security.allowFormAuthenticationForClients() // 这一步允许从requestParams中获取client-id, client-secret，设置校验client的功能
                .tokenKeyAccess("permitAll()");

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // spring security oauth2 只支持授权码和客户端凭证认证
        clients.inMemory()
                // 使用授权码模式进行授权
                .withClient("stragory")
                .secret("{noop}$2a$10$J9fGzwblYSR0TxeBNjtJC.wCyhN4cSnEHaEqYFQsdKVR7K5fWk0tu")
                .redirectUris("http://client-server:8002/api/say")
                .authorizedGrantTypes("authorization_code")
                .scopes("all", "part")
                .autoApprove("all") //作用暂时未知
                .and()

                // 使用隐式授权
                .withClient("SwordArtOnline")
                .redirectUris("http://resource-server:8003/api/hello")
                .authorizedGrantTypes("implicit")
                .autoApprove("all")
                .and()

                // 使用客户端凭证授权
                .withClient("GunGaleOnline")
                .secret("$2a$10$J9fGzwblYSR0TxeBNjtJC.wCyhN4cSnEHaEqYFQsdKVR7K5fWk0tu")
                .redirectUris("http://localhost:6666/api/ggo")
                .authorizedGrantTypes("client_credentials")
                .scopes("all")
                .and()

                // 使用用户名密码授权, 这个比较适合用于服务集群
                .withClient("alfheimonline")
                .secret("$2a$10$J9fGzwblYSR0TxeBNjtJC.wCyhN4cSnEHaEqYFQsdKVR7K5fWk0tu")
                .redirectUris("http://localhost:6666/api/alo")
                // 这种模式最主要的目的就是应用与授权服务器高度信任
                .authorizedGrantTypes("resource_owner_password_credentials")
                .scopes("all");

        // clients.withClientDetails() 这个应该是自定义clientsService
        // 还可以添加自定义扩展模式
    }

    private final DataSource dataSource;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);
        endpoints.authorizationCodeServices(jdbcAuthorizationCodeServices()); // 配置code生成，存储
        // endpoints.authenticationManager(this.authenticationManager);

        // 需要设置oauth2登录页面
        endpoints.tokenStore(new RedisTokenStore(lettuceConnectionFactory())); // 配置token生成，存储
    }

    @Bean
    public JdbcAuthorizationCodeServices jdbcAuthorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory();
        RedisStandaloneConfiguration configuration = lettuceConnectionFactory.getStandaloneConfiguration();
        configuration.setHostName("47.107.106.214");
        configuration.setDatabase(3);
        configuration.setPassword("killerWqs");
        return lettuceConnectionFactory;
    }

}
