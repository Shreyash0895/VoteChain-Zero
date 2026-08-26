package com.votechainzero.security;

import com.votechainzero.entity.Voter;
import com.votechainzero.repository.VoterRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs once per incoming request. Looks for "Authorization: Bearer <token>",
 * and if the token is valid, tells Spring Security who's making the request
 * for the rest of the request lifecycle (so @PreAuthorize etc. work in
 * controllers/services downstream).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final VoterRepository voterRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            UUID voterId = jwtService.extractVoterId(token);
            String role = jwtService.extractRole(token);

            // only set authentication if nothing is already set for this request
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<Voter> voter = voterRepository.findById(voterId);

                if (voter.isPresent()) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            voter.get(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}