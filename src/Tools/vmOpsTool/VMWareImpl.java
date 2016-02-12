import com.sun.xml.ws.client.BindingProviderProperties;
import com.vmware.vim25.*;

import javax.xml.ws.BindingProvider;
import java.util.*;

public class VMWareImpl implements IVMWare {

    // vCenter Managed Object Types
    private final String VIRTUAL_MACHINE = "VirtualMachine";
    private final String DATA_CENTER = "Datacenter";
    private final String HOST_SYSTEM = "HostSystem";
    private final String CLUSTER_COMPUTE_RESOURCE = "ClusterComputeResource";
    private final String RESOURCE_POOL = "ResourcePool";
    private final String DATA_STORE = "Datastore";

    // vCenter Managed Object properties
    private final String VM_FOLDER = "vmFolder";
    private final String PARENT = "parent";
    private final String RESOURCE_POOL_PROP = "resourcePool";
    private final String SNAPSHOT = "snapshot";
    private final String CONFIG = "config";
    private final String NAME = "name";
    private final String GUEST_IP = "guest.ipAddress";

    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private UserSession userSession;
    private ManagedObjectReference rootFolder;
    private ManagedObjectReference targetDCMor;

    public void connect(ConnectionData connData) throws Exception {
        try {
            if (!isSessionActive()) {
                System.out.println("No active session found.. establishing new session.");
                vimService = new VimService();
                vimPort = vimService.getVimPort();

                Map<String, Object> reqContext = ((BindingProvider) vimPort).getRequestContext();
                reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, connData.getUrl());
                reqContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
                reqContext.put(BindingProviderProperties.REQUEST_TIMEOUT, 30 * 60 * 1000);
                reqContext.put(BindingProviderProperties.CONNECT_TIMEOUT, 5 * 60 * 1000);
                ManagedObjectReference serviceInstance = new ManagedObjectReference();
                serviceInstance.setType("ServiceInstance");
                serviceInstance.setValue("ServiceInstance");

                if (connData.isSkipCACheck()) {
                    SkipCACheck.AllowUntrustedConnections();
    }

                serviceContent = vimPort.retrieveServiceContent(serviceInstance);
                rootFolder = serviceContent.getRootFolder();
                userSession = vimPort.login(serviceContent.getSessionManager(), connData.getUserName(), connData.getPassword(),
                        null);
                System.out.printf("Searching for datacenter with name [%s].\n", connData.getTargetDC());
                targetDCMor = getMorByName(rootFolder, connData.getTargetDC(), DATA_CENTER, false);
    }
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=USERINPUT_ConnectionFailed;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception("Failed to connect: " + exp.getMessage());
        }
        System.out.println("Successfully established session with vCenter server.");
    }

    public void cloneVMFromTemplate(String templateName, String vmName, String computeType,
                                    String computeName, String targetDS, String description, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Finding template [%s] on vCenter server.\n", templateName);
        ManagedObjectReference templateMor = getMorByName(targetDCMor, templateName, VIRTUAL_MACHINE, true);
        ManagedObjectReference targetVmFolder = (ManagedObjectReference) getMorProperties(targetDCMor, new String[]{VM_FOLDER}).get(VM_FOLDER);
        VirtualMachineCloneSpec cloneSpec = getVirtualMachineCloneSpec(computeType, computeName, targetDS);

        System.out.printf("Creating new virtual machine [%s] using template [%s].\n", vmName, templateName);
        ManagedObjectReference task = vimPort.cloneVMTask(templateMor, targetVmFolder, vmName, cloneSpec);

        if (waitAndGetTaskResult(task)) {
            System.out.printf("Successfully created virtual machine [%s] using template [%s].\n", vmName, templateName);
        } else {
            throw new Exception(
                    String.format("Failed to create virtual machine [%s] using template [%s].\n", vmName, templateName));
        }
    }

    public void createSnapshot(String vmName, String snapshotName, boolean saveVMMemory, boolean quiesceFs,
                               String description, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Creating snapshot (%s) on virtual machine (%s).\n", snapshotName, vmName);
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        ManagedObjectReference task = vimPort.createSnapshotTask(vmMor, snapshotName, description, saveVMMemory,
                quiesceFs);

        if (waitAndGetTaskResult(task)) {
            System.out.printf("Successfully created snapshot [%s] On virtual machine [%s].\n", snapshotName, vmName);
        } else {
            throw new Exception(
                    String.format("Failed to create snapshot [%s] on virtual machine [%s].\n", snapshotName, vmName));
        }
    }

    public void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Restoring snapshot (%s) on virtual machine (%s).\n", snapshotName, vmName);
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        ManagedObjectReference cpMor = getSnapshotReference(vmMor, vmName, snapshotName);
        ManagedObjectReference task = vimPort.revertToSnapshotTask(cpMor, null, true);

        if (waitAndGetTaskResult(task)) {
            System.out.printf("Successfully reverted to snapshot [%s] On virtual machine [%s].\n", snapshotName,
                    vmName);
        } else {
            throw new Exception(
                    String.format("Failed to revert snapshot [%s] on virtual machine [%s].\n", snapshotName, vmName));
        }
    }

    public void deleteSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Deleting snapshot (%s) on virtual machine (%s).\n", snapshotName, vmName);
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        ManagedObjectReference cpMor = getSnapshotReference(vmMor, vmName, snapshotName);
        ManagedObjectReference task = vimPort.removeSnapshotTask(cpMor, false, true);

        if (waitAndGetTaskResult(task)) {
            System.out.printf("Successfully deleted snapshot [%s] On virtual machine [%s].\n", snapshotName, vmName);
        } else {
            throw new Exception(
                    String.format("Failed to delete snapshot [%s] on virtual machine [%s].\n", snapshotName, vmName));
        }
    }

    public void startVM(String vmName, ConnectionData connData) throws Exception {
        connect(connData);
        if (!isVmPoweredOn(vmName, connData)) {
            ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
            ManagedObjectReference task = vimPort.powerOnVMTask(vmMor, null);

            if (!waitAndGetTaskResult(task)) {
                throw new Exception(
                        String.format("Failed to power on virtual machine [%s].\n", vmName));
            }
            System.out.printf("Waiting for virtual machine [%s] to start.\n", vmName);
            waitForVmToBoot(vmMor);
            System.out.printf("Successfully powered on virtual machine [%s].\n", vmName);
            return;
        }
        System.out.printf("Virtual machine [%s] is already running.\n", vmName);
    }

    public void deleteVM(String vmName, ConnectionData connData) throws Exception {
        connect(connData);
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        ManagedObjectReference task = vimPort.destroyTask(vmMor);

        if (waitAndGetTaskResult(task)) {
            System.out.printf("Successfully delete virtual machine [%s] from vCenter Server.\n", vmName);
        } else {
            throw new Exception(
                    String.format("Failed to delete virtual machine [%s] from vCenter Server.\n", vmName));
        }
    }

    public String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.println("Getting current snapshot name for virtual machine [ " + vmName + " ].");
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        return getCurrentSnapshotName(vmMor, vmName);
    }

    public boolean isSnapshotExists(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Finding snapshot (%s) on virtual machine (%s).\n", snapshotName, vmName);
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        try {
            getSnapshotReference(vmMor, vmName, snapshotName);
        } catch (Exception exp) {
            System.out.println(exp.getMessage());
            return false;
        }
        return true;
    }

    public boolean isVmExists(String vmName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.printf("Finding virtual machine (%s) on vCenter server.\n", vmName);
        try {
            getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        } catch (Exception exp) {
            System.err.println(exp.getMessage());
            return false;
        }
        return true;
    }

    public boolean isVmPoweredOn(String vmName, ConnectionData connData) throws Exception {
        connect(connData);
        System.out.println("Checking virtual machine [ " + vmName + " ] power status.");
        ManagedObjectReference vmMor = getMorByName(targetDCMor, vmName, VIRTUAL_MACHINE, false);
        String vmIpAddress = (String) getMorProperties(vmMor, new String[]{GUEST_IP}).get(GUEST_IP);
        return (vmIpAddress != null) && !vmIpAddress.isEmpty();
    }

    private ManagedObjectReference getMorByName(ManagedObjectReference rootContainer, String mobName, String morefType,
                                                boolean isTemplate) throws Exception {
        Map<String, ManagedObjectReference> mobrMap = getObjectsInContainerByType(rootContainer, morefType, isTemplate);

        if (!mobrMap.containsKey(mobName.toLowerCase())) {
            System.out.printf("##vso[task.logissue type=error;code=USERINPUT_ObjectNotFound;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception(morefType + " with name [ " + mobName + " ] not found.");
        }

        return mobrMap.get(mobName.toLowerCase());
    }

    private boolean waitAndGetTaskResult(ManagedObjectReference task) throws Exception {
        boolean retVal = false;

        System.out.println("Waiting for operation completion.");
        Object[] result = waitForTaskResult(task, new String[]{"info.state", "info.error"}, new String[]{"state"},
                new Object[][]{new Object[]{TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }

        return retVal;
    }

    private void waitForVmToBoot(ManagedObjectReference vmMor) throws Exception {
        String version = "";
        String ipAddress = "";
        PropertyFilterSpec filterSpec = createPropFilterSpecForObject(vmMor, new String[]{GUEST_IP});
        ManagedObjectReference propertyFilter = vimPort.createFilter(serviceContent.getPropertyCollector(), filterSpec, true);
        WaitOptions waitOptions = new WaitOptions();
        waitOptions.setMaxWaitSeconds(5 * 60); // Wait in number of seconds
        long startTime = System.currentTimeMillis();

        while (((new Date()).getTime() - startTime < 5 * 60 * 1000) && ipAddress.isEmpty()) {

            UpdateSet updateSet = vimPort.waitForUpdatesEx(serviceContent.getPropertyCollector(), version, waitOptions);
            if (updateSet == null || updateSet.getFilterSet() == null) {
                continue;
            }
            version = updateSet.getVersion();
            List<PropertyFilterUpdate> filterUpdateList = updateSet.getFilterSet();

            for (PropertyFilterUpdate filterUpdate : filterUpdateList) {
                if (filterUpdate == null || filterUpdate.getObjectSet() == null) {
                    continue;
                }
                List<ObjectUpdate> objectUpdateList = filterUpdate.getObjectSet();
                for (ObjectUpdate objectUpdate : objectUpdateList) {
                    if (objectUpdate == null || objectUpdate.getChangeSet() == null) {
                        continue;
                    }
                    List<PropertyChange> propChangeList = objectUpdate.getChangeSet();
                    for (PropertyChange propChange : propChangeList) {
                        if (propChange.getVal() != null) {
                            ipAddress = (String) propChange.getVal();
                        }
                    }
                }
            }
        }
        vimPort.destroyPropertyFilter(propertyFilter);
    }

    private Object[] waitForTaskResult(ManagedObjectReference taskMor, String[] filterProps, String[] endWaitProps,
                                       Object[][] expectedVals) throws Exception {
        try {

            Object[] endVals = new Object[endWaitProps.length];
            Object[] filterVals = new Object[filterProps.length];
            String version = "";
            PropertyFilterSpec filterSpec = createPropFilterSpecForObject(taskMor, filterProps);
            ManagedObjectReference propertyFilter = vimPort.createFilter(serviceContent.getPropertyCollector(), filterSpec, true);
            boolean reached = false;

            while (!reached) {
                UpdateSet updateSet = vimPort.waitForUpdatesEx(serviceContent.getPropertyCollector(), version, new WaitOptions());
                if (updateSet == null || updateSet.getFilterSet() == null) {
                    continue;
                }
                version = updateSet.getVersion();
                List<PropertyFilterUpdate> filterUpdateList = updateSet.getFilterSet();

                for (PropertyFilterUpdate filterUpdate : filterUpdateList) {
                    if (filterUpdate == null || filterUpdate.getObjectSet() == null) {
                        continue;
                    }
                    List<ObjectUpdate> objectUpdateList = filterUpdate.getObjectSet();
                    for (ObjectUpdate objectUpdate : objectUpdateList) {
                        if (objectUpdate == null || objectUpdate.getChangeSet() == null) {
                            continue;
                        }
                        List<PropertyChange> propChangeList = objectUpdate.getChangeSet();
                        for (PropertyChange propChange : propChangeList) {
                            updateValues(endWaitProps, endVals, propChange);
                            updateValues(filterProps, filterVals, propChange);
                        }
                    }
                }

                Object expectedVal;
                for (int chgi = 0; chgi < endVals.length && !reached; ++chgi) {
                    for (int vali = 0; vali < expectedVals[chgi].length && !reached; ++vali) {
                        expectedVal = expectedVals[chgi][vali];
                        reached = expectedVal.equals(endVals[chgi]);
                    }
                }
            }
            vimPort.destroyPropertyFilter(propertyFilter);
            return filterVals;
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_WaitForResultFailed;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception("Failed to get operation result: " + exp.getMessage());
        }
    }

    private void updateValues(String[] endWaitProps, Object[] endVals, PropertyChange propChg) {
        for (int findi = 0; findi < endWaitProps.length; findi++) {
            if (propChg.getName().lastIndexOf(endWaitProps[findi]) >= 0) {
                if (propChg.getOp() == PropertyChangeOp.REMOVE) {
                    endVals[findi] = "";
                } else {
                    endVals[findi] = propChg.getVal();
                }
            }
        }
    }

    private ManagedObjectReference getSnapshotReference(ManagedObjectReference vmMor, String vmName,
                                                        String snapshotName) throws Exception {
        System.out.printf("Querying snapshot information for virtual machine (%s).\n", vmName);
        VirtualMachineSnapshotInfo vmSnapshotInfo = (VirtualMachineSnapshotInfo) getMorProperties(vmMor,
                new String[]{SNAPSHOT}).get(SNAPSHOT);
        ManagedObjectReference snapshotMor;
        String snapshotNotFoundErr = "No snapshot found on virtual machine (" + vmName + ") with name " + snapshotName;

        if (vmSnapshotInfo != null) {
            List<VirtualMachineSnapshotTree> vmRootSnapshotList = vmSnapshotInfo.getRootSnapshotList();
            snapshotMor = findSnapshotInTree(vmRootSnapshotList, snapshotName);

            if (snapshotMor == null) {
                System.out.printf("##vso[task.logissue type=error;code=USERINPUT_SnapshotNotFound;TaskId=%s;]\n",
                        Constants.TASK_ID);
                throw new Exception(snapshotNotFoundErr);
            }
        } else {
            System.out.printf("##vso[task.logissue type=error;code=USERINPUT_SnapshotNotFound;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception(snapshotNotFoundErr);
        }
        return snapshotMor;
    }

    private String getCurrentSnapshotName(ManagedObjectReference vmMor, String vmName) throws Exception {
        System.out.printf("Querying snapshot information for virtual machine (%s).\n", vmName);
        VirtualMachineSnapshotInfo vmSnapshotInfo = (VirtualMachineSnapshotInfo) getMorProperties(vmMor,
                new String[]{SNAPSHOT}).get(SNAPSHOT);
        String currentSnapshotName = "";

        if (vmSnapshotInfo != null) {
            System.out.println("Searching for current snapshot in snapshot tree.");
            List<VirtualMachineSnapshotTree> vmRootSnapshotList = vmSnapshotInfo.getRootSnapshotList();
            ManagedObjectReference currentSnapshotMor = vmSnapshotInfo.getCurrentSnapshot();
            currentSnapshotName = getCurrentSnapshotNameFromTree(vmRootSnapshotList, currentSnapshotMor);
        } else {
            System.out.println("Snapshot info is null!!.");
        }
        return currentSnapshotName;
    }

    private String getCurrentSnapshotNameFromTree(List<VirtualMachineSnapshotTree> vmRootSnapshotList,
                                                  ManagedObjectReference currentSnapshotMor) {
        String currentSnapshot = "";

        for (VirtualMachineSnapshotTree vmSnapshot : vmRootSnapshotList) {
            System.out.printf("Current snapshot name: %s.\n", vmSnapshot.getName());
            if (vmSnapshot.getSnapshot().getValue().equals(currentSnapshotMor.getValue())) {
                System.out.println("Found current snapshot in tree ( " + vmSnapshot.getName() + " ).");
                return vmSnapshot.getName();
            } else {
                List<VirtualMachineSnapshotTree> childTree = vmSnapshot.getChildSnapshotList();
                currentSnapshot = getCurrentSnapshotNameFromTree(childTree, currentSnapshotMor);
            }
        }

        return currentSnapshot;
    }

    private ManagedObjectReference findSnapshotInTree(List<VirtualMachineSnapshotTree> vmSnapshotList,
                                                      String snapshotName) {
        ManagedObjectReference snapshotMor = null;

        for (VirtualMachineSnapshotTree vmSnapshot : vmSnapshotList) {

            if (vmSnapshot.getName().equalsIgnoreCase(snapshotName)) {
                System.out.printf("Found snapshot (%s) for virtual machine.\n", snapshotName);
                return vmSnapshot.getSnapshot();
            } else {
                List<VirtualMachineSnapshotTree> childTree = vmSnapshot.getChildSnapshotList();
                snapshotMor = findSnapshotInTree(childTree, snapshotName);
            }
        }
        return snapshotMor;
    }

    private Map<String, Object> getMorProperties(ManagedObjectReference mor, String[] propList) throws Exception {
        PropertyFilterSpec propFilterSpec = createPropFilterSpecForObject(mor, propList);
        RetrieveResult results;
        try {
            results = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), Collections.singletonList(propFilterSpec),
                    new RetrieveOptions());
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_RetriveObjectPropertiesFailed;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception("Failed to properties for managed object : " + exp.getMessage());
        }
        final Map<String, Object> propMap = new HashMap<>();
        if (results != null) {
            for (ObjectContent objContent : results.getObjects()) {
                List<DynamicProperty> properties = objContent.getPropSet();
                for (DynamicProperty property : properties) {
                    propMap.put(property.getName(), property.getVal());
                }
            }
        }
        return propMap;
    }

    private PropertyFilterSpec createPropFilterSpecForObject(ManagedObjectReference vmMor, String[] propList) {
        PropertySpec propSpec = new PropertySpec();
        propSpec.setType(vmMor.getType());
        propSpec.getPathSet().addAll(Arrays.asList(propList));
        propSpec.setAll(false);

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(vmMor);
        objSpec.setSkip(false);

        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();
        propFilterSpec.getPropSet().add(propSpec);
        propFilterSpec.getObjectSet().add(objSpec);
        return propFilterSpec;
    }

    private Map<String, ManagedObjectReference> getObjectsInContainerByType(ManagedObjectReference container,
                                                                            String morefType, boolean isTemplate) throws Exception {
        String filterProperty = morefType.equals(VIRTUAL_MACHINE) ? CONFIG : NAME;
        PropertyFilterSpec propertyFilterSpecs = createRecursiveFilterSpec(container, morefType,
                new String[]{filterProperty});

        try {
            System.out.printf("Querying %s objects on vCenter server.\n", morefType);
            RetrieveResult results = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                    Collections.singletonList(propertyFilterSpecs), new RetrieveOptions());
            String token;
            final Map<String, ManagedObjectReference> morMap = new HashMap<>();
            token = createMap(results, filterProperty, isTemplate, morMap);

            while (token != null && !token.isEmpty()) {
                results = vimPort.continueRetrievePropertiesEx(serviceContent.getPropertyCollector(), token);
                token = createMap(results, filterProperty, isTemplate, morMap);
            }
            return morMap;
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_QueryObjectsFailed;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception("Failed to fetch objects: " + exp.getMessage());
        }
    }

    private String createMap(final RetrieveResult results, String filterProperty, boolean isTemplate, final Map<String, ManagedObjectReference> morMap) {
        String token = null;

        if (results != null) {
            token = results.getToken();
            for (ObjectContent objectContent : results.getObjects()) {
                ManagedObjectReference mor = objectContent.getObj();
                List<DynamicProperty> propertySet = objectContent.getPropSet();
                if (propertySet != null && !propertySet.isEmpty() && propertySet.get(0) != null) {
                    if (filterProperty.equals(CONFIG)) {
                        VirtualMachineConfigInfo vmConfig = (VirtualMachineConfigInfo) propertySet.get(0).getVal();
                        if (vmConfig != null && vmConfig.isTemplate() == isTemplate) {
                            morMap.put(vmConfig.getName().toLowerCase(), mor);
                        }
                    } else {
                        String mobName = (String) propertySet.get(0).getVal();
                        morMap.put(mobName.toLowerCase(), mor);
                    }
                }
            }
        }
        return token;
    }

    private PropertyFilterSpec createRecursiveFilterSpec(ManagedObjectReference container, String morefType,
                                                         String[] filterProps) throws Exception {
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, container,
                    Collections.singletonList(morefType), true);

            // Create property specification to specify list properties to
            // extract from given object type
            PropertySpec propSpec = new PropertySpec();
            propSpec.setAll(false);
            propSpec.getPathSet().addAll(Arrays.asList(filterProps));
            propSpec.setType(morefType);

            // Create traversal specification to specify inventory navigation
            // path
            TraversalSpec traversalSpc = new TraversalSpec();
            traversalSpc.setName("view");
            traversalSpc.setPath("view");
            traversalSpc.setSkip(false);
            traversalSpc.setType("ContainerView");

            // Create object specification to specify root location and
            // associate a traversal specification
            ObjectSpec objSpec = new ObjectSpec();
            objSpec.setObj(containerView);
            objSpec.setSkip(false);
            objSpec.getSelectSet().add(traversalSpc);

            // Create property filter specification, then set property spec and
            // object spec
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propSpec);
            propertyFilterSpec.getObjectSet().add(objSpec);

            return propertyFilterSpec;
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_CreateFilterSpecFailed;TaskId=%s;]\n",
                    Constants.TASK_ID);
            throw new Exception("Failed to create filter spec: " + exp.getMessage());
        }
    }

    private VirtualMachineCloneSpec getVirtualMachineCloneSpec(String computeType, String computeName, String targetDS) throws Exception {

        VirtualMachineRelocateSpec relocSpec = getVirtualMachineRelocationSpec(computeType, computeName, targetDS);
        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        configSpec.setAnnotation(description);
        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        cloneSpec.setConfig(configSpec);
        cloneSpec.setLocation(relocSpec);
        cloneSpec.setPowerOn(false);
        cloneSpec.setTemplate(false);

        return cloneSpec;
    }

    private VirtualMachineRelocateSpec getVirtualMachineRelocationSpec(String computeType, String computeName, String targetDS) throws Exception {
        VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
        ManagedObjectReference targetCluster;
        ManagedObjectReference targetResourcePool;
        System.out.printf("Searching for datastore with name [%s].\n", targetDS);
        ManagedObjectReference targetDSMor = getMorByName(targetDCMor, targetDS, DATA_STORE, false);
        switch (computeType) {
            case "ESXi Host":
                ManagedObjectReference targetHost = getMorByName(targetDCMor, computeName, HOST_SYSTEM, false);
                targetCluster = (ManagedObjectReference) getMorProperties(targetHost, new String[]{PARENT}).get(PARENT);
                targetResourcePool = (ManagedObjectReference) getMorProperties(targetCluster, new String[]{RESOURCE_POOL_PROP}).get(RESOURCE_POOL_PROP);
                relocSpec.setHost(targetHost);
                break;
            case "Cluster":
                targetCluster = getMorByName(targetDCMor, computeName, CLUSTER_COMPUTE_RESOURCE, false);
                targetResourcePool = (ManagedObjectReference) getMorProperties(targetCluster, new String[]{RESOURCE_POOL_PROP}).get(RESOURCE_POOL_PROP);
                break;
            case "Resource Pool":
                targetResourcePool = getMorByName(targetDCMor, computeName, RESOURCE_POOL, false);
                break;
            default:
                System.out.printf("##vso[task.logissue type=error;code=INFRAISSUE_InvalidComputeType;TaskId=%s;]\n",
                        Constants.TASK_ID);
                throw new Exception("Invalid compute resource type: " + computeType);
        }
        relocSpec.setPool(targetResourcePool);
        relocSpec.setDatastore(targetDSMor);
        return relocSpec;
    }

    private boolean isSessionActive() {
        System.out.println("Checking for active session...");
        if (userSession == null) {
            return false;
        }
        long startTime = userSession.getLastActiveTime().toGregorianCalendar().getTime().getTime();
        return new Date().getTime() < startTime + 30 * 60 * 1000;
    }
}
