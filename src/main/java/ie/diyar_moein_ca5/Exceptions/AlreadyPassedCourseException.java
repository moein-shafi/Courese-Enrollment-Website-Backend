package ie.diyar_moein_ca5.Exceptions;

public class AlreadyPassedCourseException extends Exception {
    public AlreadyPassedCourseException() {}

    public AlreadyPassedCourseException(String message) {
        super(message);
    }
}