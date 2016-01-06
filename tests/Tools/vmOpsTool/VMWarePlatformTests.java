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

    @Test
    public void createSnapshotWithSaveVMMemoryShouldSucceed() throws Exception {
        String vmName = "VMInDC1";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, true, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
    }

    @Test
    public void createSnapshotWithQuiesceShouldSucceed() throws Exception {
        String vmName = "VMInDC2";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, true, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
    }


    // Common for restore/delete snapshot operations
    @Test
    public void restoreOrDeleteSnapshotShouldThrowIfSnapshotDoesNotExist() {
        Exception exp = null;
        try {
            vmWareImpl.restoreSnapshot("TestVM1", "InvalidSnapshot", connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();

        exp = null;
        try {
            vmWareImpl.deleteSnapshot("TestVM1", "InvalidSnapshot", connData);
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
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedIfVmIsShutdown() throws Exception {
        String vmName = "PoweredOffVM";
        String newSnapshot = "NewSnapShot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
        assertThat(vmWareImpl.snapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedIfMultipleVmWithSameNameExist() throws Exception {
        String vmName = "DuplicateVMName";
        String newSnapshot = "NewSnapShot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
        assertThat(vmWareImpl.snapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedIfTemplateAndVmHaveSameName() throws Exception {
        String vmName1 = "TemplateVM";
        String vmName2 = "VMTemplate";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName1, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        vmWareImpl.createSnapshot(vmName2, newSnapshot, false, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(newSnapshot);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName1, newSnapshot, connData);
        vmWareImpl.deleteSnapshot(vmName2, newSnapshot, connData);

        assertThat(vmWareImpl.snapshotExists(vmName1, newSnapshot, connData)).isEqualTo(false);
        assertThat(vmWareImpl.snapshotExists(vmName2, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotSucceedForVMsDiffDataCenters() throws Exception {

        String vmName1 = "VMInDC1";
        String vmName2 = "VMInDC2";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName1, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        vmWareImpl.createSnapshot(vmName2, newSnapshot, false, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(newSnapshot);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);
        vmWareImpl.restoreSnapshot(vmName2, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName2, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName1, newSnapshot, connData);
        vmWareImpl.deleteSnapshot(vmName2, newSnapshot, connData);

        assertThat(vmWareImpl.snapshotExists(vmName1, newSnapshot, connData)).isEqualTo(false);
        assertThat(vmWareImpl.snapshotExists(vmName2, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedForVMsEvenIfCaseDoesNotMatch() throws Exception {

        String vmName = "testvm1";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);

        assertThat(vmWareImpl.snapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }
}