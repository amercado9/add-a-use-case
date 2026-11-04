package team.projectpulse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team.projectpulse.user.UserService;

import java.io.IOException;

@Component
public class ActiveAccountFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final CustomBearerTokenAuthenticationEntryPoint authenticationEntryPoint;

    public ActiveAccountFilter(
            UserService userService,
            CustomBearerTokenAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.userService = userService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && authentication.isAuthenticated()
                && !this.userService.isAccountEnabled(jwtAuthentication.getName())) {
            SecurityContextHolder.clearContext();
            this.authenticationEntryPoint.commence(
                    request,
                    response,
                    new DisabledException("User account is deactivated."));
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
}
