package com.svj.exception;

import com.svj.dto.ErrorDTO;
import com.svj.dto.ServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestControllerAdvice
public class ApplicationGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceResponse<?> handleServiceException(MethodArgumentNotValidException ex){
        ServiceResponse<?> serviceResponse= new ServiceResponse<>();

        List<ErrorDTO> errorList= new LinkedList<>();
        ex.getBindingResult().getFieldErrors()
                        .forEach(error->{
                            ErrorDTO errorDTO = new ErrorDTO(String.format("%s : %s",error.getField(), error.getDefaultMessage()));
                            errorList.add(errorDTO);
                        });
        serviceResponse.setStatus(HttpStatus.BAD_REQUEST);
        serviceResponse.setErrors(errorList);
        return serviceResponse;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceResponse<?> handleServiceException(MethodArgumentTypeMismatchException ex){
        ServiceResponse<?> serviceResponse= new ServiceResponse<>();

        ErrorDTO errorDTO= new ErrorDTO(String.format("Arguement %s did not match required type of %s. ErrorMessage- %s", ex.getParameter().getParameterName(), ex.getParameter().getParameterType().toString(), ex. getMessage()));
        serviceResponse.setStatus(HttpStatus.BAD_REQUEST);
        serviceResponse.setErrors(Arrays.asList(errorDTO));
        return serviceResponse;
    }

    @ExceptionHandler(FileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServiceResponse<?> handleFileException(FileException ex){
        ServiceResponse<?> serviceResponse= new ServiceResponse<>();

        ErrorDTO errorDTO= new ErrorDTO(ex.getMessage());
        serviceResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        serviceResponse.setErrors(Arrays.asList(errorDTO));
        return serviceResponse;
    }

    @ExceptionHandler(StockProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServiceResponse<?> handleGenericErrors(StockProcessingException ex){
        ServiceResponse<?> serviceResponse= new ServiceResponse<>();

        ErrorDTO errorDTO= new ErrorDTO(ex.getMessage());
        serviceResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        serviceResponse.setErrors(Arrays.asList(errorDTO));
        return serviceResponse;
    }


}
