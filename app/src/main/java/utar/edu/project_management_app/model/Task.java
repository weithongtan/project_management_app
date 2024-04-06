package utar.edu.project_management_app.model;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class Task {

    private String taskId;
    private String taskName;
    private String dueDate;
    private String timeCreation = LocalDateTime.now().toString();
    private Priority priority;
    private Section section;
    private String description;
    private Project project; // A task belongs to a project



    enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
    enum Section {
        TODO,
        PENDING,
        DONE
    }

    // Constructors, getters, and setters
    public Task() {
    }

    public Task(String taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
