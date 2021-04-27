package ie.diyar_moein_ca5.Classes;

import ie.diyar_moein_ca5.Exceptions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public HashMap<String, AddedOffering> getAddedOfferings() {
        return addedOfferings;
    }

    public int getUnits() {
        int units = 0;
        for (AddedOffering offering : addedOfferings.values()) {
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

        public void makeFinalize() {
            if (this.status == Status.non_finalized)
                this.course.increaseSignedUp();

            this.status = Status.finalized;
        }

        public Status getFinalized() {
            return status;
        }

        public Course getCourse() {
            return this.course;
        }

        public String getStatus() {
            if (isWaiting)
                return "Waiting";
            return "Enrolled";
        }

        public void setToRemove() {
            this.wantsToRemove = true;
        }

        public void cancelRemoving() {
            this.wantsToRemove = false;
        }

        public boolean isWantsToRemove() {
            return wantsToRemove;
        }

        public void changeWaitingToFalse() { this.isWaiting = false; }
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

    public void addToWeeklySchedule(Course course, boolean waiting) throws ClassesTimeCollisionException, ExamsTimeColisionException, AlreadyAddedCourseToPlanException {
        for (AddedOffering offering1 : addedOfferings.values()) {
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

        if (this.addedOfferings.containsKey(course.getCode()))
            this.addedOfferings.get(course.getCode()).cancelRemoving();

        else {
            AddedOffering addedOffering = new AddedOffering(course, waiting);
            this.addedOfferings.put(course.getCode(), addedOffering);
        }
    }

    public LocalTime stringToLocalTime(String time) {
        if (!time.contains(":"))
            time = time + ":00";

        String[] classTime = time.split(":");
        if (classTime[0].length() < 2)
            classTime[0] = "0" + classTime[0];

        return LocalTime.parse(classTime[0] + ":" + classTime[1]);
    }

    public void removeFromWeeklySchedule(String code) throws CourseNotFoundException {
        if (addedOfferings.containsKey(code)) {
            if (addedOfferings.get(code).getFinalized() == Status.finalized)
                addedOfferings.get(code).getCourse().decreaseSignedUp();
            addedOfferings.remove(code);
        }
        else
            throw new CourseNotFoundException(code);
    }

    public int getFinalizedUnits() {
        finalizedUnits = 0;
        for (AddedOffering offering : addedOfferings.values())
            if (offering.getFinalized() == Status.finalized && offering.getStatus().equals("Enrolled"))
                finalizedUnits += offering.course.getUnits();

        return finalizedUnits;
    }

    public void calculateGpa() throws CourseNotFoundException {
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

    public boolean checkFinalizeConditions() throws PrerequisiteException, CourseCapacityException, MinimumRequiredUnitsException, MaximumAllowedUnitsException, AlreadyPassedCourseException {
        Integer totalUnits = 0;
        for (AddedOffering offering : this.addedOfferings.values()) {
            Course course = offering.getCourse();
            if (offering.isWantsToRemove())
                continue;
            for (String prerequisite : course.getPrerequisites()) {

                for (HashMap<String, Double> grades : this.termGrades.values()) {
                    if (grades.containsKey(prerequisite)) {
                        if (grades.get(prerequisite) < 10) {
                            throw new PrerequisiteException(course.getName());
                        }
                    } else {
                        throw new PrerequisiteException(course.getName());
                    }
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

    public void removeWaitingStatus(Course course) {
        if (this.addedOfferings.containsKey(course.getCode()))
            this.addedOfferings.get(course.getCode()).changeWaitingToFalse();   }
    }