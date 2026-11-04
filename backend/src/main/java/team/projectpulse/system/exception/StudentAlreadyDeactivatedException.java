package team.projectpulse.system.exception;

public class StudentAlreadyDeactivatedException extends RuntimeException {

    public StudentAlreadyDeactivatedException(Integer studentId) {
        super("Student " + studentId + " is already deactivated.");
    }
}
