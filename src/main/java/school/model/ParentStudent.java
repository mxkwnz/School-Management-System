package school.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parent_student")
public class ParentStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String parentUserId;

    @Column(nullable = false)
    private String studentUserId;

    public ParentStudent() {}

    public ParentStudent(String parentUserId, String studentUserId) {
        this.parentUserId = parentUserId;
        this.studentUserId = studentUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }
}

