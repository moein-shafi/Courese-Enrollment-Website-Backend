package ie.diyar_moein_ca5.controllers.Requests;

public class JWTDeleteCourseRequest {
    private String courseCode;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
}
