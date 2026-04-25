package br.com.bolao.web.controller;

import br.com.bolao.service.PredictionService.PredictionClosedException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PredictionClosedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public String handlePredictionClosed(PredictionClosedException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/prediction-closed";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/bad-request";
    }
}
