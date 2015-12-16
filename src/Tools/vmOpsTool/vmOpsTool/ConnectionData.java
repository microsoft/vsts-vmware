package vmOpsTool;

public class ConnectionData {

    public ConnectionData(String vCenterUrl, String vCenterUserName, String vCenterPassword) {
        this.userName = vCenterUserName;
        this.password = vCenterPassword;
        this.url = vCenterUrl;
    }

    public String userName;
    public String password;
    public String url;
}
