package com.showsync.config;

import com.showsync.security.JwtUtil;
import com.showsync.security.UserPrincipal;
import com.showsync.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time group chat functionality.
 * Enables STOMP messaging with JWT authentication integration.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    /**
     * Configure message broker for handling messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Register STOMP endpoints for WebSocket connections
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure based on your frontend origin
                .withSockJS();
    }
    
    /**
     * Configure client inbound channel with JWT authentication interceptor
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from CONNECT frame
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);
                        
                        try {
                            // Validate JWT token
                            String username = jwtUtil.extractUsername(token);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            if (jwtUtil.validateToken(token, userDetails)) {
                                // Create authentication and set user
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                
                                accessor.setUser(authentication);
                                
                                // Store user ID for easy access in message handlers
                                if (userDetails instanceof UserPrincipal) {
                                    UserPrincipal userPrincipal = (UserPrincipal) userDetails;
                                    accessor.setSessionAttributes(java.util.Map.of(
                                        "userId", userPrincipal.getId(),
                                        "username", userPrincipal.getUsername()
                                    ));
                                }
                                
                                logger.debug("WebSocket authentication successful for user: {}", username);
                            } else {
                                logger.warn("Invalid JWT token in WebSocket connection");
                                throw new IllegalArgumentException("Invalid token");
                            }
                        } catch (ExpiredJwtException e) {
                            logger.warn("Expired JWT token in WebSocket connection: {}", e.getMessage());
                            throw new IllegalArgumentException("Token expired");
                        } catch (Exception e) {
                            logger.error("Error authenticating WebSocket connection: {}", e.getMessage());
                            throw new IllegalArgumentException("Authentication failed");
                        }
                    } else {
                        logger.warn("No valid Authorization header in WebSocket CONNECT frame");
                        throw new IllegalArgumentException("Missing authentication token");
                    }
                }
                
                return message;
            }
        });
    }
    
    /**
     * Utility method to extract user ID from WebSocket session
     */
    public static Long getUserIdFromSession(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            Object userId = headerAccessor.getSessionAttributes().get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
    
    /**
     * Utility method to extract username from WebSocket session
     */
    public static String getUsernameFromSession(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            Object username = headerAccessor.getSessionAttributes().get("username");
            if (username instanceof String) {
                return (String) username;
            }
        }
        return null;
    }
} 