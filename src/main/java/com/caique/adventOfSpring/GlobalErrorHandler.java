package com.caique.adventOfSpring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public class GlobalErrorHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        try {
            return next.handle(request);
        } catch (CustomerNotFoundException e) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
            problemDetail.setTitle("Customer Not Found");
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(problemDetail);
        } catch (Exception e) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            problemDetail.setTitle("Internal Server Error");
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
        }
    }

    @Override
    public HandlerFilterFunction<ServerResponse, ServerResponse> andThen(HandlerFilterFunction<ServerResponse, ServerResponse> after) {
        return HandlerFilterFunction.super.andThen(after);
    }

    @Override
    public HandlerFunction<ServerResponse> apply(HandlerFunction<ServerResponse> handler) {
        return HandlerFilterFunction.super.apply(handler);
    }
}
