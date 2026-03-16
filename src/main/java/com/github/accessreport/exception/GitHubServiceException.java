package com.github.accessreport.exception;

public class GitHubServiceException extends RuntimeException {

    private final String errorMessage;

    public GitHubServiceException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
