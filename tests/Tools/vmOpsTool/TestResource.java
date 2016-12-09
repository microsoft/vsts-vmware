class TestResource {

    String Template;
    String NewVmName;
    String TargetDC;
    String ComputeType;
    String ComputeName;
    String Datastore;
    String CustomizationSpec;
    
    private static final String USER_NAME = System.getProperty("user.name");
    
    private static final String SUBRAHMANYAM_MANDAVILLI = "Subrahmanyam Mandavilli";
    private static final String CHAITANYA_SHRIKHANDE = "Chaitanya Shrikhande";
    private static final String KRISHNA_ADITYA = "Krishna Aditya";
    private static final String ARUN_MAHAPATRA = "Arun Mahapatra";
    private static final String RAMI_ABUGHAZALEH = "rami.abughazaleh";
    
    static int GetOperationTimeoutValue()
    {
        switch (USER_NAME) {
            case RAMI_ABUGHAZALEH:
                // <editor-fold defaultstate="collapsed" desc="Rami's operation timeout value">
                return 0;
                // </editor-fold>
            default:
                return 1200;
        }
    }
    
    static TestResource GetTestResource(String testMethodName) throws Exception
    {
        String errorMessage = "Test resource not found for user '" + USER_NAME + "' and test method name '" + testMethodName + "'";
        TestResource testResource = new TestResource();
        
        switch (USER_NAME) {
            case SUBRAHMANYAM_MANDAVILLI:
            case CHAITANYA_SHRIKHANDE:
            case KRISHNA_ADITYA:
            case ARUN_MAHAPATRA:
                // <editor-fold defaultstate="collapsed" desc="Microsoft's Test Resources">
                String ubuntuTemplate = "Ubuntu";
                String linuxCustomizationSpec = "Linux Spec";
                
                switch (testMethodName){
                    case "cloneVMFromTemplateWithTargetComputeAsESXiHostShouldSucceed":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVmOnEsxiHost";
                        testResource.TargetDC = "redmonddc";
                        testResource.ComputeType = "ESXi Host";
                        testResource.ComputeName = "idcvstt-lab318.corp.microsoft.com";
                        testResource.Datastore = "datastore1";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithTargetComputeAsClusterShouldSucceed":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVmOnCluster";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Cluster";
                        testResource.ComputeName = "fareastcluster";
                        testResource.Datastore = "SharedStorage";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithTargetComputeAsResourcePoolShouldSucceed":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVmOnResourcePool";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "fareastrp";
                        testResource.Datastore = "SharedStorage";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithInvalidTargetComputeShouldFail":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Invalid Compute";
                        testResource.ComputeName = "fareastrp";
                        testResource.Datastore = "datastore2";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromNonExistingTemplateShouldFail":
                        testResource.Template = "InvalidTemplate";
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "fareastrp";
                        testResource.Datastore = "datastore2";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetDatacenterShouldFail":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "InvalidDc";
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "fareastrp";
                        testResource.Datastore = "datastore1";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetESXiShouldFail":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "ESXi Host";
                        testResource.ComputeName = "InvalidHost";
                        testResource.Datastore = "datastore1";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetCloudShouldFail":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Cluster";
                        testResource.ComputeName = "InvalidCluster";
                        testResource.Datastore = "datastore1";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetResourcePoolShouldFail":
                        testResource.Template = ubuntuTemplate;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "fareastdc";
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "InvalidRp";
                        testResource.Datastore = "datastore1";
                        testResource.CustomizationSpec = linuxCustomizationSpec;
                        break;
                    default:
                        throw new Exception(errorMessage);
                }
                break;
                // </editor-fold>
            case RAMI_ABUGHAZALEH:
                // <editor-fold defaultstate="collapsed" desc="Rami's Test Resources">
                String templateName = "win2012r2-standard";
                String datacenterName = "Datacenter";
                String datastoreName = "Datastore";
                
                switch (testMethodName){
                    case "cloneVMFromTemplateWithTargetComputeAsESXiHostShouldSucceed":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVmOnEsxiHost";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "ESXi Host";
                        testResource.ComputeName = "192.168.0.209";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithTargetComputeAsClusterShouldSucceed":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVmOnCluster";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Cluster";
                        testResource.ComputeName = "Cluster";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithTargetComputeAsResourcePoolShouldSucceed":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVmOnResourcePool";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "Resource Pool";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithInvalidTargetComputeShouldFail":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Invalid Compute";
                        testResource.ComputeName = "Resource Pool";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromNonExistingTemplateShouldFail":
                        testResource.Template = "InvalidTemplate";
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "Resource Pool";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetDatacenterShouldFail":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = "InvalidDc";
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "Resource Pool";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetESXiShouldFail":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "ESXi Host";
                        testResource.ComputeName = "InvalidHost";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetCloudShouldFail":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Cluster";
                        testResource.ComputeName = "InvalidCluster";
                        testResource.Datastore = "Datastore";
                        break;
                    case "cloneVMFromTemplateWithNonExistingTargetResourcePoolShouldFail":
                        testResource.Template = templateName;
                        testResource.NewVmName = "newVM";
                        testResource.TargetDC = datacenterName;
                        testResource.ComputeType = "Resource Pool";
                        testResource.ComputeName = "InvalidRp";
                        testResource.Datastore = "Datastore";
                        break;
                    default:
                        throw new Exception(errorMessage);
                }
                break;
                // </editor-fold>
            default:
                throw new Exception(errorMessage);
        }
        
        return testResource;
    }
    
    static TestVirtualMachine GetVirtualMachine(String testMethodName) throws Exception
    {
        String errorMessage = "Test virtual machine not found for user '" + USER_NAME + "' and test method name '" + testMethodName + "'";
        TestVirtualMachine vm = new TestVirtualMachine();
        
        switch (USER_NAME) {
            case SUBRAHMANYAM_MANDAVILLI:
            case CHAITANYA_SHRIKHANDE:
            case KRISHNA_ADITYA:
            case ARUN_MAHAPATRA:
                // <editor-fold defaultstate="collapsed" desc="Microsoft's Test Virtual Machines">
                String snapshotOne = "Snapshot1";
                String snapshotTwo = "Snapshot2";
                
                switch (testMethodName){
                    case "createSnapshotWithSaveVMMemoryShouldSucceed":
                        vm.Name = "Win8";
                        vm.NewSnapshot = "NewSnapshot";
                        vm.TargetDC = "fareastdc";
                        break;
                    case "createSnapshotWithQuiesceShouldSucceed":
                        vm.Name = "Win7";
                        vm.NewSnapshot = "NewSnapshot";
                        vm.TargetDC = "redmonddc";
                        break;
                    case "startAndStopOnAVmShouldNotThrow":
                        vm.Name = "startAndStopWindows";
                        vm.TargetDC = "redmonddc";
                        break;
                    case "startAndStopVmShouldSucceedForLinuxVM":
                        vm.Name = "startAndStopUbuntu";
                        vm.TargetDC = "fareastdc";
                        break;
                    case "powerOnAndPowerOffVmShouldSucceed":
                        vm.Name = "powerOnAndPowerOff";
                        vm.TargetDC = "fareastdc";
                        break;
                    case "restoreOrDeleteSnapshotShouldThrowIfSnapshotDoesNotExist":
                        vm.Name = "Win2012R2";
                        vm.Snapshot = "InvalidSnapshot";
                        vm.TargetDC = "redmonddc";
                        break;
                    case "restoreSnapshotShouldThrowIfVmDoesNotExist":
                        vm.Name = "InvalidVM";
                        vm.Snapshot = snapshotOne;
                        vm.TargetDC = "redmonddc";
                        break;
                    case "restoreOrCreateOrDeleteSnapshotShouldSucceedIfVmIsShutdown":
                        vm.Name = "PoweredOffVM";
                        vm.NewSnapshot = "NewSnapShot";
                        vm.Snapshot = snapshotTwo;
                        vm.TargetDC = "redmonddc";
                        break;
                    case "restoreSnapshotShouldThrowIfMultipleVmsWithSameNameExistInADC":
                        vm.Name = "DuplicateVM";
                        vm.Snapshot = snapshotOne;
                        vm.TargetDC = "fareastdc";
                        break;
                    case "restoreOrCreateOrDeleteSnapshotShouldSucceedForVMsEvenIfCaseDoesNotMatch":
                        vm.Name = "win2012r2";
                        vm.NewSnapshot = "NewSnapshot";
                        vm.Snapshot = snapshotTwo;
                        vm.TargetDC = "redmonddc";
                        break;
                    default:
                        throw new Exception(errorMessage);
                }
                break;
                // </editor-fold>
            case RAMI_ABUGHAZALEH:
                // <editor-fold defaultstate="collapsed" desc="Rami's Test Virtual Machines">
                String virtualMachineName = "Win2012R2";
                String datacenterName = "Datacenter";
                
                switch (testMethodName){
                    case "createSnapshotWithSaveVMMemoryShouldSucceed":
                        vm.Name = virtualMachineName;
                        vm.NewSnapshot = "NewSnapshot";
                        vm.TargetDC = datacenterName;
                        break;
                    case "createSnapshotWithQuiesceShouldSucceed":
                        vm.Name = virtualMachineName;
                        vm.NewSnapshot = "NewSnapshot";
                        vm.TargetDC = datacenterName;
                        break;
                    case "startAndStopOnAVmShouldNotThrow":
                        vm.Name = virtualMachineName;
                        vm.TargetDC = datacenterName;
                        break;
                    case "startAndStopVmShouldSucceedForLinuxVM":
                        vm.Name = "UbuntuServer14.04";
                        vm.TargetDC = datacenterName;
                        break;
                    case "powerOnAndPowerOffVmShouldSucceed":
                        vm.Name = virtualMachineName;
                        vm.TargetDC = datacenterName;
                        break;
                    case "restoreOrDeleteSnapshotShouldThrowIfSnapshotDoesNotExist":
                        vm.Name = virtualMachineName;
                        vm.Snapshot = "InvalidSnapshot";
                        vm.TargetDC = datacenterName;
                        break;
                    case "restoreSnapshotShouldThrowIfVmDoesNotExist":
                        vm.Name = "InvalidVM";
                        vm.Snapshot = "Snapshot1";
                        vm.TargetDC = datacenterName;
                        break;
                    case "restoreOrCreateOrDeleteSnapshotShouldSucceedIfVmIsShutdown":
                        vm.Name = virtualMachineName;
                        vm.NewSnapshot = "NewSnapShot";
                        vm.Snapshot = "Snapshot2";
                        vm.TargetDC = datacenterName;
                        break;
                    case "restoreSnapshotShouldThrowIfMultipleVmsWithSameNameExistInADC":
                        vm.Name = "Win2012R2Duplicate";
                        vm.Snapshot = "Snapshot1";
                        vm.TargetDC = datacenterName;
                        break;
                    case "restoreOrCreateOrDeleteSnapshotShouldSucceedForVMsEvenIfCaseDoesNotMatch":
                        vm.Name = "win2012r2";
                        vm.NewSnapshot = "NewSnapshot";
                        vm.Snapshot = "Snapshot2";
                        vm.TargetDC = datacenterName;
                        break;
                    default:
                        throw new Exception(errorMessage);
                }
                break;
                // </editor-fold>
            default:
                throw new Exception(errorMessage);
        }
        
        return vm;
    }
    
    static ConnectionData GetConnectionData(String vCenterUrl, String testMethodName) throws Exception {
        
        String vCenterUserName;
        String vCenterPassword;
        String targetDC;
                
        switch (USER_NAME) {
            case SUBRAHMANYAM_MANDAVILLI:
            case CHAITANYA_SHRIKHANDE:
            case KRISHNA_ADITYA:
            case ARUN_MAHAPATRA:
                // <editor-fold defaultstate="collapsed" desc="Microsoft's Connection Data">
                vCenterUserName = "Administrator@vsphere.local";
                vCenterPassword = "Password~1";
                targetDC = "fareastdc";
                break;
                // </editor-fold>
            case RAMI_ABUGHAZALEH:
                // <editor-fold defaultstate="collapsed" desc="Rami's Connection Data">
                vCenterUserName = "Administrator@vsphere.local";
                vCenterPassword = "password";
                targetDC = "Datacenter";
                break;
                // </editor-fold>
            default:
                throw new Exception("Test connection data not found for user '" + USER_NAME + "'");
        }
        
        switch (testMethodName){
            case "connectShouldThrowForInvalidCredentials":
                return new ConnectionData(vCenterUrl, vCenterUserName, "InvalidPassword", targetDC, true);
            case "connectShouldThrowWithoutSkipCACheck":
                return new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword, targetDC, false);
            default:
                return new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword, targetDC, true);
        }
    }
}
