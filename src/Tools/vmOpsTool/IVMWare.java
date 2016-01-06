
public interface IVMWare {

    /**
     *
     * @param vmName
     *            name of the virtual machine
     * @param snapshotName
     *            name of the snapshot to create
     * @param saveVMMemory
     *            save virtual machine memory
     * @param quiesceFs
     *            quiesce virtual machine file system
     * @param description
     *            snapshot description
     * @throws Exception
     *             on failure
     */
    public void createSnapshot(String vmName, String snapshotName, boolean saveVMMemory, boolean quiesceFs,
            String description, ConnectionData connData) throws Exception;

    /**
     * Takes a comma separated list of virtual machines and restores given
     * snapshot for them
     * 
     * @param vmName
     *            name of the virtual machine
     * @param snapshotName
     *            Name of the snapshot to restore
     * @param connData
     *            connection information for vCenter
     * @throws Exception
     *             on failure
     */
    public void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     *
     * @param vmName
     *            name of the virtual machine
     * @param snapshotName
     *            name of the snapshot to be deleted
     * @param connData
     *            connection information for vCenter
     * @throws Exception
     */
    public void deleteSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * Gets the current active snapshot information for the VM
     * 
     * @param vmName
     *            Name of the virtual machine
     * @return current snapshot name
     * @throws Exception
     *             on operation failure
     */
    public String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception;

    /**
     *
     * @param vmName
     *            name of the virtual machine
     * @param snapshotName
     *            name of the snapshot to be found
     * @param connData
     *            connection information for vCenter
     * @return true if found otherwise false
     * @throws Exception
     */
    public boolean snapshotExists(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * 
     * @param connData
     *            for connecting to vCenter Server
     * @throws Exception
     *             on failure to connect
     */
    public void connect(ConnectionData connData) throws Exception;
}
