package net.deuce.moman.om;

public class TypeMismatchException extends RuntimeException {
  public TypeMismatchException() {
  }

  public TypeMismatchException(String message) {
    super(message);
  }

  public TypeMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  public TypeMismatchException(Throwable cause) {
    super(cause);
  }
}
