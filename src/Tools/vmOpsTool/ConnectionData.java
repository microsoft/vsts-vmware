

public class ConnectionData {

    public ConnectionData(String vCenterUrl, String vCenterUserName, String vCenterPassword, boolean skipCACheck) {
        this.userName = vCenterUserName;
        this.password = vCenterPassword;
        this.url = vCenterUrl;
        this.skipCACheck = skipCACheck;
    }

    public String userName;
    public String password;
    public String url;
    public boolean skipCACheck;
}
