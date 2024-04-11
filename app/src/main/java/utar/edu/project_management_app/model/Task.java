package utar.edu.project_management_app.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task implements Serializable {

    private String taskId;
    private String taskName;
    private String dueDate;
    private String timeCreation;
    private String priority;
    private String section;
    private String description;
    private String projectId; // A task belongs to a project
    private List<String> userId; // A task has many users


    // Constructors, getters, and setters
    public Task() {
    }

    public Task(String taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
    }

    public Task(String taskId, String taskName, String dueDate, String priority, String section, String description, String projectId) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.priority = priority;
        this.section = section;
        this.description = description;
        this.projectId = projectId;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.timeCreation = LocalDateTime.now().format(formatter);
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

    public String  getTimeCreation() {

        return timeCreation;
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

    public void addTaskToDatabase(){

    }
}
