package com.yoursummoner.api.model.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

//Classe de erros do projeto
public class ApiError extends Exception {

    //Classe de origem do erro
    private Class clazz;

    //Método de origem do erro
    private String method;

    //Descrição do erro
    private String description;

    //Status da resposta
    //Seja da Api consumida
    //Seja de validações ou erros criados nas services
    private HttpStatus httpStatus;

    //Corpo do erro, seja criado dentro da API ou de uma Exception qualquer
    private Object apiError;

    //API a qual foi requisitada
    private String searchUrl;


    /**
     * @param clazz
     * @param method
     * @param description
     * @param apiError
     * @param httpStatus
     * @param searchUrl
     * @apiNote Construtor para erros em request, usado no sendReceive
     */
    public ApiError(Class clazz, String method, String description, Object apiError, HttpStatus httpStatus, String searchUrl) {
        this.clazz = clazz;
        this.method = method;
        this.description = description;
        this.apiError = apiError;
        this.httpStatus = httpStatus;
        this.searchUrl = searchUrl;
    }


    /**
     * @param clazz
     * @param method
     * @param description
     * @param httpStatus
     * @apiNote Validações, não necessáriamente vai resultar em erro
     */
    public ApiError(Class clazz, String method, String description, HttpStatus httpStatus) {
        this.clazz = clazz;
        this.method = method;
        this.description = description;
        this.httpStatus = httpStatus;
        this.searchUrl = "";
    }


    /**
     * @param clazz
     * @param method
     * @param description
     * @param bodyErrorRequest
     * @apiNote Construtor para erros genéricos nas classes
     */
    public ApiError(Class clazz, String method, String description, Object bodyErrorRequest) {
        this.clazz = clazz;
        this.method = method;
        this.description = description;
        this.apiError = bodyErrorRequest;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.searchUrl = "";
    }


    public ApiError() {
    }

    //Retira throwable
    @JsonIgnore
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    //Retira StackTraceElement[]
    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    //Retira Message
    @JsonIgnore
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    //Retira localizedMessage
    @JsonIgnore
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @JsonIgnore
    @Override
    public synchronized Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getApiError() {
        return apiError;
    }

    public void setApiError(String apiError) {
        this.apiError = apiError;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setApiError(Object apiError) {
        this.apiError = apiError;
    }
}
