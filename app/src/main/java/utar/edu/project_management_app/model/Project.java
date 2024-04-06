package utar.edu.project_management_app.model;


import java.util.List;

public class Project {
    private String projectId;
    private String projectName;
    private String dueDate;
    private String timeCreation;
    private List<Task> tasks; // A project has many tasks
    private List<User> users; // A project has many users
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

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
