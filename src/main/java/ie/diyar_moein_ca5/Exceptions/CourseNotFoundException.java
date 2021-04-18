package ie.diyar_moein_ca5.Exceptions;

public class CourseNotFoundException extends Exception {
    public CourseNotFoundException() {}

    public CourseNotFoundException(String message) {
        super(message);
    }
}