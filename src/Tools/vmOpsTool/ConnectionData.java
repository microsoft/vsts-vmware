public class ConnectionData {

    private String userName;
    private String password;
    private String url;
    private boolean skipCACheck;

    public ConnectionData(String vCenterUrl, String vCenterUserName, String vCenterPassword, boolean skipCACheck) {
        this.userName = vCenterUserName;
        this.password = vCenterPassword;
        this.url = vCenterUrl;
        this.skipCACheck = skipCACheck;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSkipCACheck() {
        return skipCACheck;
    }
}
