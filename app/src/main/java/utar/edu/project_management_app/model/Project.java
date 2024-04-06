package utar.edu.project_management_app.model;


import java.util.List;

public class Project {
    private String projectId;
    private String projectName;
    private String dueDate;
    private String timeCreation;
    private List<String> taskId; // A project has many tasks
    private List<String> userId; // A project has many users
    // Constructors, getters, and setters
    public Project() {
    }

    public Project(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

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

    public String getTimeCreation() {
        return timeCreation;
    }

    public List<String> getTaskId() {
        return taskId;
    }

    public void setTaskId(List<String> taskId) {
        this.taskId = taskId;
    }

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }
}
