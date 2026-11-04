package team.projectpulse.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import team.projectpulse.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActiveAccountFilterTest {

    @Mock
    UserService userService;

    @Mock
    CustomBearerTokenAuthenticationEntryPoint authenticationEntryPoint;

    @InjectMocks
    ActiveAccountFilter activeAccountFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activeBearerAccountContinues() throws Exception {
        authenticate("j.smith@abc.edu");
        given(this.userService.isAccountEnabled("j.smith@abc.edu")).willReturn(true);
        MockFilterChain chain = new MockFilterChain();

        this.activeAccountFilter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        verify(this.authenticationEntryPoint, never()).commence(any(), any(), any());
    }

    @Test
    void deactivatedBearerAccountIsClearedAndRejected() throws Exception {
        authenticate("j.smith@abc.edu");
        given(this.userService.isAccountEnabled("j.smith@abc.edu")).willReturn(false);
        MockFilterChain chain = new MockFilterChain();

        this.activeAccountFilter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNull();
        verify(this.authenticationEntryPoint).commence(any(), any(), any());
    }

    private static void authenticate(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }
}
