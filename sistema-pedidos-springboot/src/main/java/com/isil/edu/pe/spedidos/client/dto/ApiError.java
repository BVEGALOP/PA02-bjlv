package com.isil.edu.pe.spedidos.client.dto;

/**
 * Estructura de error publicada por inventario-service.
 */
public class ApiError {

  private int status;
  private String error;
  private String message;
  private String path;
  private String timestamp;

  public ApiError() {
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
