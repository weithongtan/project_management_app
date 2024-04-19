package utar.edu.project_management_app;

public class ReadWriteUserDetails {
    public String username;
    public String email;
    public String fcmtoken;

    public ReadWriteUserDetails(){}
    public ReadWriteUserDetails(String username, String email, String fcmtoken){
        this.username = username;
        this.email = email;
        this.fcmtoken = fcmtoken;
    }
}
