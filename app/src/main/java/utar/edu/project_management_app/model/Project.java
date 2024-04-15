package utar.edu.project_management_app.model;


import java.util.ArrayList;
import java.util.List;

public class Project {
    private String projectId;
    private String projectName;
    private String dueDate;
    private List<String> taskId;
    private List<String> emails = new ArrayList<>();

    public Project() {
    }

    public Project(String projectId, String projectName, String dueDate, List<String> taskId, List<String> emails) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.dueDate = dueDate;
        this.taskId = taskId;
        this.emails = emails;
    }

    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public List<String> getTaskId() {
        return taskId;
    }

    public void setTaskId(List<String> taskId) {
        this.taskId = taskId;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}

