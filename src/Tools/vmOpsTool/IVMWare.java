
public interface IVMWare {

    /**
     * @param vmName       name of the virtual machine
     * @param snapshotName name of the snapshot to create
     * @param saveVMMemory save virtual machine memory
     * @param quiesceFs    quiesce virtual machine file system
     * @param description  snapshot description
     * @throws Exception on failure
     */
    void createSnapshot(String vmName, String snapshotName, boolean saveVMMemory, boolean quiesceFs,
                        String description, ConnectionData connData) throws Exception;

    /**
     * Takes a comma separated list of virtual machines and restores given
     * snapshot for them
     *
     * @param vmName       name of the virtual machine
     * @param snapshotName Name of the snapshot to restore
     * @param connData     connection information for vCenter
     * @throws Exception on failure
     */
    void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * @param vmName       name of the virtual machine
     * @param snapshotName name of the snapshot to be deleted
     * @param connData     connection information for vCenter
     * @throws Exception
     */
    void deleteSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * Gets the current active snapshot information for the VM
     *
     * @param vmName Name of the virtual machine
     * @return current snapshot name
     * @throws Exception on operation failure
     */
    String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception;

    /**
     * @param vmName       name of the virtual machine
     * @param snapshotName name of the snapshot to be found
     * @param connData     connection information for vCenter
     * @return true if found otherwise false
     * @throws Exception
     */
    boolean isSnapshotExists(String vmName, String snapshotName, ConnectionData connData) throws Exception;

    /**
     * @param vmName   name of the virtual machine
     * @param connData connection information for vCenter
     * @return true if found otherwise false
     * @throws Exception
     */
    boolean isVmExists(String vmName, ConnectionData connData) throws Exception;

    /**
     * @param vmName name of the virtual machine
     * @param connData connection information for vCenter
     * @return true if powered on, otherwise false
     * @throws Exception
     */
    boolean isVmPoweredOn(String vmName, ConnectionData connData) throws Exception;

    /**
     * @param templateName   name of the virtual machine template to be cloned
     * @param vmName         name of the virtual machine
     * @param computeType    type of the compute esxi/cluster/resourcepool
     * @param computeName    name of the compute resouce
     * @param description    optional description for create operation
     * @param connData       connection information for vCenter
     * @throws Exception
     */
    void cloneVMFromTemplate(String templateName, String vmName, String computeType, String computeName, String datastore, String description, ConnectionData connData) throws Exception;

    /**
     * @param vmName   name of the virtual machine
     * @param connData vCenter connection information
     * @throws Exception
     */
    void deleteVM(String vmName, ConnectionData connData) throws Exception;

    /**
     * @param connData for connecting to vCenter Server
     * @throws Exception on failure to connect
     */
    void connect(ConnectionData connData) throws Exception;

    /**
     * @param vmName   name of the virtual machine
     * @param connData vCenter connection information
     * @throws Exception
     */
    void startVM(String vmName, ConnectionData connData) throws Exception;
}
