package utar.edu.project_management_app.model;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class Task {

    private String taskId;
    private String taskName;
    private String dueDate;
    private String timeCreation = LocalDateTime.now().toString();
    private String priority;
    private String section;
    private String description;
    private String projectId; // A task belongs to a project



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

    public Task(String taskId, String taskName, String dueDate, String priority, String section, String description, String project) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.priority = priority;
        this.section = section;
        this.description = description;
        this.projectId = project;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTimeCreation() {
        return timeCreation;
    }

    public String getProject() {
        return projectId;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }
}
