package vmOpsTool;

public interface IVMWare {

    //Takes a comma separated list of virtual machines and restores given snapshot for them
    //param vmList : comma separated list of virtual machines as string
    //param snapShotName : Name of the snapshot to create
    //return int : return status of the operation    
    public void RestoreSnapShot(String vmList, String snapshotName, ConnectionData connData) throws Exception;

}
