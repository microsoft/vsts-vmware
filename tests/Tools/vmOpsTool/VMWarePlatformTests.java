import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public abstract class VMWarePlatformTests {
    private String vCenterUserName = "Administrator@vsphere.local";
    private String vCenterPassword = "Password~1";
    private String vCenterUrl = getvCenterUrl();
    private ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);
    private IVMWare vmWareImpl = getVmWareImpl();
    private String snapshotOne = "Snapshot1";
    private String snapshotTwo = "Snapshot2";

    public abstract IVMWare getVmWareImpl();

    public abstract String getvCenterUrl();

    // Common for all Operations
    // The following two scenarios needs to be validated during stress testing
    // restoreSnapshotShouldUseASessionTimeoutOfSixtyMinutes
    // restoreSnapshotShouldThrowIfWaitTimesOut

    @Test
    public void connectShouldThrowConnectionToServerFailsAuthentication() {
        Exception exp = null;
        try {
            vmWareImpl.connect(new ConnectionData(vCenterUrl, vCenterUserName, "InvalidPassword"));
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void connectShouldNotThrowOnSuccessfulAuthentication() throws Exception {
        vmWareImpl.connect(connData);
    }

    // Common for restore/delete snapshot operations
    @Test
    public void restoreSnapshotShouldThrowIfSnapshotDoesNotExist() {
        Exception exp = null;
        try {
            vmWareImpl.restoreSnapshot("TestVM1", "InvalidSnapshot", connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    // Common for Snapshot operations
    @Test
    public void restoreSnapshotShouldThrowIfVmDoesNotExist() {
        Exception exp = null;
        try {
            vmWareImpl.restoreSnapshot("InvalidVM", snapshotOne, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void restoreSnapshotShouldRestoreSnapshotIfVmIsShutdown() throws Exception {
        String vmName = "PoweredOffVM";
        vmWareImpl.restoreSnapshot(vmName, snapshotOne, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotOne);
        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);
    }

    @Test
    public void restoreSnapshotShouldRestoreSnapshotIfMultipleVmWithSameNameExist() throws Exception {
        String vmName = "DuplicateVMName";
        vmWareImpl.restoreSnapshot(vmName, snapshotOne, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotOne);
        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);
    }

    @Test
    public void restoreSnapshotShouldRestoreSnapshotIfTemplateAndVmHaveSameName() throws Exception {
        String vmName1 = "TemplateVM";
        String vmName2 = "VMTemplate";
        vmWareImpl.restoreSnapshot(vmName1, snapshotOne, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotOne, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotOne);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotOne);

        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotTwo);
    }

    @Test
    public void restoreSnapshotShouldRestoreSnapshotForVMsDiffDataCenters() throws Exception {

        String vmName1 = "VMInDC1";
        String vmName2 = "VMInDC2";

        vmWareImpl.restoreSnapshot(vmName1, snapshotOne, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotOne, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotOne);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotOne);

        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotTwo);
    }

    @Test
    public void restoreSnapshotShouldRestoreSnapshotForVMsEvenIfCaseDoesNotMatch() throws Exception {

        String vmName1 = "testvm1";

        vmWareImpl.restoreSnapshot(vmName1, snapshotOne, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotOne);

        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);

    }
}