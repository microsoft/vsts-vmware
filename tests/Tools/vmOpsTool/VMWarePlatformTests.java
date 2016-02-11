import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public abstract class VMWarePlatformTests {
    private String vCenterUserName = "Administrator@vsphere.local";
    private String vCenterPassword = "Password~1";
    private String vCenterUrl = getvCenterUrl();
    private ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword, true);
    private IVMWare vmWareImpl = getVmWareImpl();
    private String snapshotOne = "Snapshot1";
    private String templateName = "Ubuntu";
    private String snapshotTwo = "Snapshot2";

    public abstract IVMWare getVmWareImpl();

    public abstract String getvCenterUrl();

    // Common for all Operations
    // The following two scenarios needs to be validated during stress testing
    // restoreSnapshotShouldUseASessionTimeoutOfSixtyMinutes
    // restoreSnapshotShouldThrowIfWaitTimesOut
    // cloneVMFromTemplateWithInsufficientDiskSpaceShouldFail
    // cloneVMFromTemplateWithInsufficientComputeShouldFail

    // Clone VM from template action tests
    @Test
    public void cloneVMFromTemplateWithTargetComputeAsESXiHostShouldSucceed() throws Exception {
        String newVmName = "newVmOnEsxiHost";
        String targetLocation = "redmonddc";
        String computeType = "ESXi Host";
        String computeName = "idcvstt-lab318.corp.microsoft.com";
        String datastore = "datastore1";
        String description = "Creating new VM from ubuntuVM template on ESXi host";

        vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);

        assertThat(vmWareImpl.isVmExists(newVmName, connData)).isEqualTo(true);

        vmWareImpl.deleteVM(newVmName, connData);
    }

    @Test
    public void cloneVMFromTemplateWithTargetComputeAsClusterShouldSucceed() throws Exception {
        String newVmName = "newVmOnCluster";
        String targetLocation = "fareastdc";
        String computeType = "Cluster";
        String computeName = "fareastcluster";
        String datastore = "SharedStorage";
        String description = "Creating new VM from ubuntuVM template on cluster";

        vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);

        assertThat(vmWareImpl.isVmExists(newVmName, connData)).isEqualTo(true);

        vmWareImpl.deleteVM(newVmName, connData);
    }

    @Test
    public void cloneVMFromTemplateWithTargetComputeAsResourcePoolShouldSucceed() throws Exception {
        String newVmName = "newVmOnResourcePool";
        String targetLocation = "fareastdc";
        String computeType = "Resource Pool";
        String computeName = "fareastrp";
        String datastore = "SharedStorage";
        String description = "Creating new VM from ubuntuVM template on resource pool";

        vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);

        assertThat(vmWareImpl.isVmExists(newVmName, connData)).isEqualTo(true);

        vmWareImpl.deleteVM(newVmName, connData);
    }

    @Test
    public void cloneVMFromTemplateWithInvalidTargetComputeShouldFail() throws Exception {
        String newVmName = "newVM";
        String targetLocation = "fareastdc";
        String computeType = "Invalid Compute";
        String computeName = "fareastrp";
        String datastore = "datastore2";
        String description = "Creating new VM with invalid template";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void cloneVMFromNonExistingTemplateShouldFail() throws Exception {
        String newVmName = "newVM";
        String nonExistingTemplate = "InvalidTemplate";
        String targetLocation = "fareastdc";
        String computeType = "Resource Pool";
        String computeName = "fareastrp";
        String datastore = "datastore2";
        String description = "Creating new VM with invalid template";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(nonExistingTemplate, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void cloneVMFromTemplateWithNonExistingTargetDatacenterShouldFail() throws Exception {
        String newVmName = "newVM";
        String targetLocation = "InvalidDc";
        String computeType = "Resource Pool";
        String computeName = "fareastrp";
        String datastore = "datastore1";
        String description = "Creating new VM from ubuntuVM template on invalid data center";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void cloneVMFromTemplateWithNonExistingTargetESXiShouldFail() {
        String newVmName = "newVM";
        String targetLocation = "fareastdc";
        String computeType = "ESXi Host";
        String computeName = "InvalidHost";
        String datastore = "datastore1";
        String description = "Creating new VM from ubuntuVM template on non existing ESXi host";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void cloneVMFromTemplateWithNonExistingTargetCloudShouldFail() {
        String newVmName = "newVM";
        String targetLocation = "fareastdc";
        String computeType = "Cluster";
        String computeName = "InvalidCluster";
        String datastore = "datastore1";
        String description = "Creating new VM from ubuntuVM template on non existing cluster";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void cloneVMFromTemplateWithNonExistingTargetResourcePoolShouldFail() {
        String newVmName = "newVM";
        String targetLocation = "fareastdc";
        String computeType = "Resource Pool";
        String computeName = "InvalidRp";
        String datastore = "datastore1";
        String description = "Creating new VM from ubuntuVM template on non existing resource pool";

        Exception exp = null;
        try {
            vmWareImpl.cloneVMFromTemplate(templateName, newVmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void connectShouldThrowForInvalidCredentials() {
        Exception exp = null;
        try {
            vmWareImpl.connect(new ConnectionData(vCenterUrl, vCenterUserName, "InvalidPassword", true));
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void connectShouldThrowWithoutSkipCACheck() {
        Exception exp = null;
        try {
            vmWareImpl.connect(new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword, false));
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
        String vmName = "Win8";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, true, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
    }

    @Test
    public void createSnapshotWithQuiesceShouldSucceed() throws Exception {
        String vmName = "Win7";
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
            vmWareImpl.restoreSnapshot("Win2012R2", "InvalidSnapshot", connData);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();

        exp = null;
        try {
            vmWareImpl.deleteSnapshot("Win2012R2", "InvalidSnapshot", connData);
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
        assertThat(vmWareImpl.isSnapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedIfMultipleVmWithSameNameExist() throws Exception {
        String vmName = "Win10";
        String newSnapshot = "NewSnapShot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);
        assertThat(vmWareImpl.isSnapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedIfTemplateAndVmHaveSameName() throws Exception {
        String vmName1 = "UbuntuVM";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName1, newSnapshot, false, false, "Snapshot created during platform tests", connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(newSnapshot);


        vmWareImpl.restoreSnapshot(vmName1, snapshotTwo, connData);
        assertThat(vmWareImpl.getCurrentSnapshot(vmName1, connData)).isEqualTo(snapshotTwo);


        vmWareImpl.deleteSnapshot(vmName1, newSnapshot, connData);
        assertThat(vmWareImpl.isSnapshotExists(vmName1, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotSucceedForVMsDiffDataCenters() throws Exception {

        String vmName1 = "Win8";
        String vmName2 = "Win7";
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

        assertThat(vmWareImpl.isSnapshotExists(vmName1, newSnapshot, connData)).isEqualTo(false);
        assertThat(vmWareImpl.isSnapshotExists(vmName2, newSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void restoreOrCreateOrDeleteSnapshotShouldSucceedForVMsEvenIfCaseDoesNotMatch() throws Exception {

        String vmName = "win2012r2";
        String newSnapshot = "NewSnapshot";

        vmWareImpl.createSnapshot(vmName, newSnapshot, false, false, "Snapshot created during platform tests", connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(newSnapshot);

        vmWareImpl.restoreSnapshot(vmName, snapshotTwo, connData);

        assertThat(vmWareImpl.getCurrentSnapshot(vmName, connData)).isEqualTo(snapshotTwo);

        vmWareImpl.deleteSnapshot(vmName, newSnapshot, connData);

        assertThat(vmWareImpl.isSnapshotExists(vmName, newSnapshot, connData)).isEqualTo(false);
    }
}