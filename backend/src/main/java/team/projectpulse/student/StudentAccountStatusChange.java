package team.projectpulse.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class StudentAccountStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statusChangeId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private LocalDateTime effectiveAt;

    public StudentAccountStatusChange() {
    }

    public StudentAccountStatusChange(Student student, boolean enabled, LocalDateTime effectiveAt) {
        this.student = student;
        this.enabled = enabled;
        this.effectiveAt = effectiveAt;
    }

    public Integer getStatusChangeId() {
        return statusChangeId;
    }

    public void setStatusChangeId(Integer statusChangeId) {
        this.statusChangeId = statusChangeId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getEffectiveAt() {
        return effectiveAt;
    }

    public void setEffectiveAt(LocalDateTime effectiveAt) {
        this.effectiveAt = effectiveAt;
    }
}
