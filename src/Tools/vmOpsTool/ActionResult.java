public class ActionResult {
    private String errorMessage;
    private String failedVm;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFailedVm() {
        return failedVm;
    }

    public void setFailedVm(String failedVm) {
        this.failedVm = failedVm;
    }
}
