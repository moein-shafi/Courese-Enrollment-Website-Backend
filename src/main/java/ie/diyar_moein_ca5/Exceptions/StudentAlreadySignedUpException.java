package ie.diyar_moein_ca5.Exceptions;
public class StudentAlreadySignedUpException extends Exception {
    public StudentAlreadySignedUpException() {}

    public StudentAlreadySignedUpException(String message) {
        super(message);
    }
}
