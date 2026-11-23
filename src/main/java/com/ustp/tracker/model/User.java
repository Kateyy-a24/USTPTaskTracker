package com.ustp.tracker.model;

/** A student account. Inherits the name and email from {@link Person}. */
public class User extends Person {
    private int id;
    private String username;
    private String studentId;
    private String college;
    private String program;
    private String semester;
    private String avatarPath;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    /** A student is shown by full name, or by username when no full name is saved. */
    @Override
    public String getDisplayName() {
        String name = getFullName();
        return name == null || name.isBlank() ? username : name;
    }

    /** Two User objects are the same student when they have the same id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id == ((User) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
