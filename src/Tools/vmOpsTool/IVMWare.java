

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
     * Gets the current active snapshot information for the VM
     * @param vmName Name of the virtual machine
     * @return current snapshot name
     * @throws Exception 
     */
    public String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception;
    
    /**
     * 
     * @param connData
     * @throws Exception
     */
    public void connect(ConnectionData connData) throws Exception;
}
