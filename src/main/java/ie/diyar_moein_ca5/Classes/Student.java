package ie.diyar_moein_ca5.Classes;

import ie.diyar_moein_ca5.Exceptions.*;
import ie.diyar_moein_ca5.repository.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;


public class Student {
    final private String studentId;
    final private String name, secondName;
    final private String birthDate;
    private int finalizedUnits = 0;
    private float gpa;
    private int totalPassedUnits;
    private HashMap<String, AddedOffering> addedOfferings = new HashMap<>();
    private HashMap<Integer, HashMap<String, Double>> termGrades = new HashMap<>();
    private HashMap<Integer, Double> termGpa = new HashMap<>();
    private String field;
    private String faculty;
    private String level;
    private String status;
    private String img;

    public HashMap<Integer, HashMap<String, Double>> getTermGrades() {
        return termGrades;
    }

    public HashMap<Integer, Double> getTermGpa() {
        return termGpa;
    }

    public void addTermGrade(HashMap<Integer, HashMap<String, Double>> termGrades) {
        this.termGrades = termGrades;
    }

    public float getGpa() {
        return gpa;
    }

    public int getTotalPassedUnits() {
        return totalPassedUnits;
    }

    public String getField() {
        return field;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public String getImg() {
        return img;
    }

    public enum Status {
        finalized,
        non_finalized
    }

    public HashMap<String, AddedOffering> getAddedOfferings() throws SQLException, CourseNotFoundException {
        HashMap<String, AddedOffering> addedOfferingHashMap = new HashMap<>();
        for (AddedOffering addedOffering : getAddedOfferingsFromDB()) {
            addedOfferingHashMap.put(addedOffering.course.getCode(), addedOffering);
        }
        return  addedOfferingHashMap;
    }

    public int getUnits() throws SQLException, CourseNotFoundException {
        int units = 0;
        for (AddedOffering offering : getAddedOfferingsFromDB()) {
            if (offering.isWantsToRemove())
                continue;
            units += offering.course.getUnits();
        }
        return units;
    }

    public class AddedOffering {
        Course course;
        Status status = Status.non_finalized;
        boolean wantsToRemove = false;
        boolean isWaiting = false;

        public AddedOffering(Course course, boolean waiting) {
            this.course = course;
            this.isWaiting = waiting;
        }

        public void makeFinalize() throws SQLException {
            this.status = Status.finalized;
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("update %s set status = ? where studentId = ? and courseCode = ? and classCode = ? and isWaiting = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setString(1, "finalized");
            statement.setString(2, studentId);
            statement.setString(3, course.getCode());
            statement.setString(4, course.getClassCode());
            statement.setBoolean(5, false);
            int result = statement.executeUpdate();
            statement.close();
            connection.close();

        }

        public String getFinalized() throws SQLException {
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("select status from %s where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setString(1, studentId);
            statement.setString(2, course.getCode());
            statement.setString(3, course.getClassCode());
            ResultSet result = statement.executeQuery();
            String status = "";
            if (result.next())
                status = result.getString("status");
            statement.close();
            connection.close();
            return status;
        }

        public Course getCourse() {
            return this.course;
        }

        public String getStatus() throws SQLException {
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("select isWaiting from %s where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setString(1, studentId);
            statement.setString(2, course.getCode());
            statement.setString(3, course.getClassCode());
            ResultSet result = statement.executeQuery();
            boolean isWaiting = true;
            if (result.next())
                isWaiting = result.getBoolean("isWaiting");
            statement.close();
            connection.close();
            if (isWaiting)
                return "Waiting";
            return "Enrolled";
        }

        public void setToRemove() throws SQLException {
            this.wantsToRemove = true;

            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("update %s set wantsToRemove = ? where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setBoolean(1, true);
            statement.setString(2, studentId);
            statement.setString(3, course.getCode());
            statement.setString(4, course.getClassCode());
            int result = statement.executeUpdate();
            statement.close();
            connection.close();

        }

        public void cancelRemoving() throws SQLException {
            this.wantsToRemove = false;
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("update %s set wantsToRemove = ? where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setBoolean(1, false);
            statement.setString(2, studentId);
            statement.setString(3, course.getCode());
            statement.setString(4, course.getClassCode());
            int result = statement.executeUpdate();
            statement.close();
            connection.close();

        }

        public boolean isWantsToRemove() throws SQLException {
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("select wantsToRemove from %s where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setString(1, studentId);
            statement.setString(2, course.getCode());
            statement.setString(3, course.getClassCode());
            ResultSet result = statement.executeQuery();
            boolean wantsToRemove = false;
            if (result.next())
                wantsToRemove = result.getBoolean("wantsToRemove");
            statement.close();
            connection.close();
            return wantsToRemove;
        }

        public void changeWaitingToFalse() throws SQLException {
            this.isWaiting = false;
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    String.format("update %s set isWaiting = ? where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
            statement.setBoolean(1, false);
            statement.setString(2, studentId);
            statement.setString(3, course.getCode());
            statement.setString(4, course.getClassCode());
            int result = statement.executeUpdate();
            statement.close();
            connection.close();
        }
    }

    public Student(String studentId, String name, String secondName, String birthDate, String field, String faculty,
                   String level, String status, String img) {
        this.studentId = studentId;
        this.name = name;
        this.secondName = secondName;
        this.birthDate = birthDate;
        this.field = field;
        this.faculty = faculty;
        this.level = level;
        this.status = status;
        this.img = img;
    }

    public String getStudentId() {
        return this.studentId;
    }

    public String getBirthDate() {
        return this.birthDate;
    }

    public String getName() {
        return this.name;
    }

    public String getSecondName() {
        return this.secondName;
    }

    private ArrayList<AddedOffering> getAddedOfferingsFromDB() throws SQLException, CourseNotFoundException {
        ArrayList<AddedOffering> addedOfferings = new ArrayList<>();
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select * from %s a where a.studentId = ?;", Database.getDatabase().getAddedOfferingTableName()));
        statement.setString(1, this.studentId);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            String courseCode = result.getString("courseCode");
            String classCode = result.getString("classCode");
            String status = result.getString("status");
            boolean wantsToRemove = result.getBoolean("wantsToRemove");
            boolean isWaiting = result.getBoolean("isWaiting");
            Course course = Database.getDatabase().getCourse(courseCode, classCode);
            addedOfferings.add(new AddedOffering(course, isWaiting));
        }
        result.close();
        statement.close();
        connection.close();
        return addedOfferings;
    }

    public void addToWeeklySchedule(Course course, boolean waiting) throws ClassesTimeCollisionException, ExamsTimeColisionException, AlreadyAddedCourseToPlanException, SQLException, CourseNotFoundException {
        for (AddedOffering offering1 : getAddedOfferingsFromDB()) {
            if (offering1.isWantsToRemove())
                continue;

            if (offering1.getCourse().getCode().equals(course.getCode()))
                throw new AlreadyAddedCourseToPlanException(course.getName());

            LocalDateTime examStart1 = offering1.course.getExamTime().get("start");
            LocalDateTime examEnd1 = offering1.course.getExamTime().get("end");
            LocalDateTime examStart2 = course.getExamTime().get("start");
            LocalDateTime examEnd2 = course.getExamTime().get("end");
            if (examStart1.isBefore(examEnd2) && examStart2.isBefore(examEnd1))
                throw new ExamsTimeColisionException(course.getName() + " and " + offering1.getCourse().getName());

            String[] classTimes1 = offering1.course.getClassTime().split("-");
            LocalTime start1 = stringToLocalTime(classTimes1[0]);
            LocalTime end1 = stringToLocalTime(classTimes1[1]);
            boolean checkTimes = false;
            for (String day : offering1.course.getClassDays()) {
                if (course.getClassDays().contains(day)) {
                    checkTimes = true;
                    break;
                }
            }
            if (!checkTimes)
                continue;
            String[] classTimes2 = course.getClassTime().split("-");
            LocalTime start2 = stringToLocalTime(classTimes2[0]);
            LocalTime end2 = stringToLocalTime(classTimes2[1]);
            if (start1 == start2 || end1 == end2) {
                throw new ClassesTimeCollisionException(course.getName() + " and " + offering1.getCourse().getName());
            }
        }

        if (checkRepeatedAddedOffering(course.getCode(), course.getClassCode(), this.studentId))
            cancelAddedOfferingRemoving(course.getCode(), course.getClassCode(), this.studentId);

        else {
            addAddedOfferingToDB(course, waiting);
        }
    }

    private void addAddedOfferingToDB(Course course, boolean isWaiting) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("insert into %s (courseCode, classCode, studentId, status, wantsToRemove, isWaiting)"
                        + " values (?, ?, ?, ?, ?, ?);", Database.getDatabase().getAddedOfferingTableName()));
        statement.setString(1, course.getCode());
        statement.setString(2, course.getClassCode());
        statement.setString(3, studentId);
        statement.setString(4, "non_finalized");
        statement.setBoolean(5, false);
        statement.setBoolean(6, isWaiting);

        int result = statement.executeUpdate();
        statement.close();
        connection.close();
    }

    private void cancelAddedOfferingRemoving(String courseCode, String classCode, String studentId) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("update %s set wantsToRemove = ? where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
        statement.setBoolean(1, false);
        statement.setString(2, studentId);
        statement.setString(3, courseCode);
        statement.setString(4, classCode);
        int result = statement.executeUpdate();
        statement.close();
        connection.close();
    }

    private boolean checkRepeatedAddedOffering(String courseCode, String classCode, String studentId) throws SQLException {
        Connection connection = ConnectionPool.getConnection();

        String command = String.format("select * from %s a where a.studentId = ? and a.courseCode = ? and a.classCode = ?;", Database.getDatabase().getAddedOfferingTableName());
        if (classCode == "")
            command = String.format("select * from %s a where a.studentId = ? and a.courseCode = ?;", Database.getDatabase().getAddedOfferingTableName());

        PreparedStatement statement = connection.prepareStatement(command);
        statement.setString(1, studentId);
        statement.setString(2, courseCode);
        if (classCode != "")
            statement.setString(3, classCode);

        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return exist;
    }

    public LocalTime stringToLocalTime(String time) {
        if (!time.contains(":"))
            time = time + ":00";

        String[] classTime = time.split(":");
        if (classTime[0].length() < 2)
            classTime[0] = "0" + classTime[0];

        return LocalTime.parse(classTime[0] + ":" + classTime[1]);
    }

    private AddedOffering getOneAddedOfferingFromDB(String courseCode) throws SQLException, CourseNotFoundException {
        AddedOffering addedOffering = null;
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select * from %s a where a.studentId = ? and a.courseCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
        statement.setString(1, this.studentId);
        statement.setString(2, courseCode);

        ResultSet result = statement.executeQuery();
        if (result.next()) {
            String classCode = result.getString("classCode");
            String status = result.getString("status");
            boolean wantsToRemove = result.getBoolean("wantsToRemove");
            boolean isWaiting = result.getBoolean("isWaiting");
            Course course = Database.getDatabase().getCourse(courseCode, classCode);
            addedOffering = new AddedOffering(course, isWaiting);
        }
        result.close();
        statement.close();
        connection.close();
        return addedOffering;
    }

    private void removeAddedOfferingFromWeekly(AddedOffering addedOffering) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("delete from %s where studentId = ? and courseCode = ? and classCode = ?;", Database.getDatabase().getAddedOfferingTableName()));
        statement.setString(1, this.studentId);
        statement.setString(2, addedOffering.getCourse().getCode());
        statement.setString(3, addedOffering.getCourse().getClassCode());

        int result = statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public void removeFromWeeklySchedule(String code) throws CourseNotFoundException, SQLException {
        if (checkRepeatedAddedOffering(code, "", studentId)) {
            AddedOffering addedOffering = getOneAddedOfferingFromDB(code);
            removeAddedOfferingFromWeekly(addedOffering);
        }
        else
            throw new CourseNotFoundException(code);
    }

    public int getFinalizedUnits() throws SQLException, CourseNotFoundException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("Select sum(units) from %s a, Courses c where (a.courseCode = c.courseCode) and (studentId = ?) and (status = 'finalized') and (isWaiting = 0);", Database.getDatabase().getAddedOfferingTableName()));
        statement.setString(1, this.studentId);
        ResultSet result = statement.executeQuery();
        if (result.next())
            this.finalizedUnits = result.getInt("sum(units)");
        statement.close();
        connection.close();
        return this.finalizedUnits;
    }

    public void calculateGpa() throws CourseNotFoundException, SQLException {
        Integer units, total_units = 0;
        Double sum_of_grades = 0.0;
        for (Integer termNumber : this.termGrades.keySet()) {
            Integer term_units = 0;
            Double sum_of_term_grades = 0.0;
            for (String code : this.termGrades.get(termNumber).keySet()) {
                units = Database.getDatabase().getCourse(code).getUnits();
                if (this.termGrades.get(termNumber).get(code) >= 10)
                    totalPassedUnits += units;

                total_units += units;
                term_units += units;
                sum_of_term_grades += this.termGrades.get(termNumber).get(code) * units;
                sum_of_grades += this.termGrades.get(termNumber).get(code) * units;
            }
            this.termGpa.put(termNumber, (double) (sum_of_term_grades / term_units));
            term_units = 0;
            sum_of_term_grades = 0.0;
        }
        this.gpa = (float) (sum_of_grades / total_units);
    }

    public boolean checkFinalizeConditions() throws PrerequisiteException, CourseCapacityException, MinimumRequiredUnitsException, MaximumAllowedUnitsException, AlreadyPassedCourseException, SQLException, CourseNotFoundException {
        Integer totalUnits = 0;
        for (AddedOffering offering : getAddedOfferingsFromDB()) {
            Course course = offering.getCourse();
            if (offering.isWantsToRemove())
                continue;
            if (offering.getFinalized().equals("finalized")) {
                totalUnits += course.getUnits();
                continue;
            }
            for (String prerequisite : course.getPrerequisites()) {
                boolean passed = false;
                for (HashMap<String, Double> grades : this.termGrades.values()) {
                    System.out.println(grades);
                    if (grades.containsKey(prerequisite)) {
                        if (grades.get(prerequisite) >= 10) {
                            passed = true;
                        }
                    }
                }
                if (!passed)
                {
                    throw new PrerequisiteException(course.getName());
                }

            }

            for (HashMap<String, Double> grades : this.termGrades.values())
                if (grades.containsKey(course.getCode()))
                    throw new AlreadyPassedCourseException(course.getName());

            if (course.getSignedUp() >= course.getCapacity())
                throw new CourseCapacityException(course.getName());

            totalUnits += course.getUnits();
        }
        if (totalUnits < 12)
            throw new MinimumRequiredUnitsException();

        if (totalUnits > 20)
            throw new MaximumAllowedUnitsException();

        return true;
    }

    public void removeWaitingStatus(Course course) throws SQLException, CourseNotFoundException {
        if (checkRepeatedAddedOffering(course.getCode(), "", studentId))
            getOneAddedOfferingFromDB(course.getCode()).changeWaitingToFalse();   }
    }