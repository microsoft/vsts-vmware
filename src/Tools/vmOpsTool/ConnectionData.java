public class ConnectionData {

    private String userName;
    private String password;
    private String url;
    private String targetDC;
    private boolean skipCACheck;

    public ConnectionData(String vCenterUrl, String vCenterUserName, String vCenterPassword, String vCenterTargetDC, boolean skipCACheck) {
        this.userName = vCenterUserName;
        this.password = vCenterPassword;
        this.url = vCenterUrl;
        this.skipCACheck = skipCACheck;
        this.targetDC = vCenterTargetDC;
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

    public String getTargetDC() {
        return targetDC;
    }

    public void setTargetDC(String vCenterTargetDC) {
        this.targetDC = vCenterTargetDC;
    }
}
