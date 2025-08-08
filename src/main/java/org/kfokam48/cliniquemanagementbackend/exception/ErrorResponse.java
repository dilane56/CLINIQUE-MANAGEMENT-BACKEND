package org.kfokam48.cliniquemanagementbackend.exception;

import lombok.Getter;

// Dans un package comme com.example.yourapp.exception
@Getter
public class ErrorResponse {
    private int status;
    private String message;
    private long timestamp;
    private String path;

    // Constructeurs, getters et setters
    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
