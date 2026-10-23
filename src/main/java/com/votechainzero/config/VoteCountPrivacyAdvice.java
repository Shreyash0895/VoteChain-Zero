package com.votechainzero.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.votechainzero.controller.ElectionController;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Set;


@RestControllerAdvice(assignableTypes = ElectionController.class)
public class VoteCountPrivacyAdvice implements ResponseBodyAdvice<Object> {

    private static final Set<String> HIDDEN = Set.of("minedVotes", "pendingVotes", "totalVotes");

    private final ObjectMapper mapper;

    public VoteCountPrivacyAdvice(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || isAdmin()) {
            return body;
        }
        try {
            JsonNode tree = mapper.valueToTree(body);
            strip(tree);
            return tree;
        } catch (IllegalArgumentException e) {
            return body;
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void strip(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            obj.remove(HIDDEN);
            obj.elements().forEachRemaining(this::strip);
        } else if (node instanceof ArrayNode arr) {
            arr.elements().forEachRemaining(this::strip);
        }
    }
}