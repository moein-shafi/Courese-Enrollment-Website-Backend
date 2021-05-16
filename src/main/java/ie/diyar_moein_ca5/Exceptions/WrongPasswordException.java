package ie.diyar_moein_ca5.Exceptions;

public class WrongPasswordException extends Exception {
    public WrongPasswordException() {}

    public WrongPasswordException(String message) {
        super(message);
    }
}
