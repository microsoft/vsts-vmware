package vmOpsTool;

public interface IVMWare {

    /**
     * Takes a comma separated list of virtual machines and restores given snapshot for them
     * @param vmList comma separated list of virtual machines as string
     * @param snapshotName Name of the snapshot to create
     * @param connData connection information for vCenter
     * @throws Exception on failure
     */
    public void restoreSnapshot(String vmList, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * Checks whether snapshot exists on given VM
     * @param vmName name of the virtual machine
     * @param snapshotName name of the snapshot to check
     * @return true if snapshot exists, else false
     */
    public Boolean snapshotExists(String vmName, String snapshotName);
}
