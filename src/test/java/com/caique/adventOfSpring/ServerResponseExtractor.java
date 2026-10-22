package com.caique.adventOfSpring;

import jakarta.servlet.ServletException;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.List;

public class ServerResponseExtractor {

    public static String extractBody(ServerResponse serverResponse)
            throws ServletException, IOException {

        MockHttpServletResponse httpServletResponse =
                new MockHttpServletResponse();

        MockHttpServletRequest httpServletRequest =
                new MockHttpServletRequest();

        ServerResponse.Context context =
                new ServerResponseExtractor.DebugServerContext();

        serverResponse.writeTo(
                httpServletRequest,
                httpServletResponse,
                context
        );

        return httpServletResponse.getContentAsString();
    }

    public static class DebugServerContext
            implements ServerResponse.Context {

        @Override
        public List<HttpMessageConverter<?>> messageConverters() {
            return List.of(
                    new MappingJackson2HttpMessageConverter()
            );
        }
    }
}