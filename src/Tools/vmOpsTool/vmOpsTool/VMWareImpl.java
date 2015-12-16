package vmOpsTool;

import java.util.*;
import javax.xml.ws.BindingProvider;
import com.vmware.vim25.*;

public class VMWareImpl implements IVMWare {

    private static final String taskId = "735d144e-55fe-44d6-b687-db9031b6e70b";
    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private UserSession userSession;
    private ManagedObjectReference rootFolder;

    public void RestoreSnapShot(String vmList, String snapshotName, ConnectionData connData) throws Exception {

        Init(connData);
        String [] vmNames = vmList.split(",");
        String failedVmList = "";

        for (String vmName : vmNames) {
            System.out.printf("Restoring snapshot(%s) on virtual machine (%s)", snapshotName, vmName);
            Map<String, ManagedObjectReference> vmsMap = getObjectsInContainerByType(this.rootFolder, "VirtualMachine");
            
            if(!vmsMap.containsKey(vmName))
            {
                System.out.printf("##vso[task.logissue type=error;code=USERINPUT_VmNotFound;TaskId=%s;]", taskId);
                System.err.println("Virtual machine with name " + vmName + "not found.");
                failedVmList += vmName;
                continue;
            }
            
            ManagedObjectReference vmMor = vmsMap.get(vmName);
            ManagedObjectReference cpMor = getSnapshotReference(vmMor, vmName, snapshotName);
            ManagedObjectReference task = vimPort.revertToSnapshotTask(cpMor, null, true);
            
            if (waitAndGetTaskResult(task)) {
                System.out.printf("Successfully reverted to snapshot [%s] On virtual machine [%s]", snapshotName, vmName);
            } else {
                System.err.printf("Failed to revert snapshot [%s] on virtual machine [%s]", snapshotName, vmName) ;
                failedVmList += vmName;
            }
        }
        
        if(!failedVmList.isEmpty()) {
            System.err.printf("Failed to revert snapshot [%s] on virtual machines [%s]", snapshotName, failedVmList);
            throw new Exception();
        }
        return;
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

    private Object[] waitForTaskResult(ManagedObjectReference taskMor, String[] filterProps, String[] endWaitProps, Object[][] expectedVals)
            throws Exception {
        try {

            Object[] endVals = new Object[endWaitProps.length];
            Object[] filterVals = new Object[filterProps.length];
            String version = "";
            PropertyFilterSpec filterSpec = createPropFilterSpecForObject(taskMor, filterProps);
            ManagedObjectReference filterSpecRef = vimPort.createFilter(serviceContent.getPropertyCollector(), filterSpec, true);
            boolean reached = false;

            while (!reached) {
                UpdateSet updateSet = vimPort.waitForUpdatesEx(serviceContent.getPropertyCollector(), version, new WaitOptions());
                if (updateSet == null || updateSet.getFilterSet() == null) {
                    continue;
                }
                version = updateSet.getVersion();
                List<PropertyFilterUpdate> filterUpdateList = updateSet.getFilterSet();

                for (PropertyFilterUpdate filterUpdate : filterUpdateList) {
                    if(filterUpdate == null || filterUpdate.getObjectSet() == null) {
                        continue;
                    }
                    List<ObjectUpdate> objectUpdateList = filterUpdate.getObjectSet();
                    for (ObjectUpdate objectUpdate : objectUpdateList) {
                        if(objectUpdate == null || objectUpdate.getChangeSet() == null) {
                            continue;
                        }
                        List<PropertyChange> propChangeList = objectUpdate.getChangeSet();
                        for (PropertyChange propChange : propChangeList) {
                            updateValues(endWaitProps, endVals, propChange);
                            updateValues(filterProps, filterVals, propChange);
                        }
                    }
                }

                Object expectedVal = null;
                for (int chgi = 0; chgi < endVals.length && !reached; ++chgi) {
                    for (int vali = 0; vali < expectedVals[chgi].length && !reached; ++vali) {
                        expectedVal = expectedVals[chgi][vali];
                        reached = expectedVal.equals(endVals[chgi]) || reached;
                    }
                }
            }
            vimPort.destroyPropertyFilter(filterSpecRef);
            return filterVals;
        }
        catch(Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_WaitForResultFailed;TaskId=%s;]", taskId);
            System.err.println("Failed to get operation result: " + exp.getMessage());
            throw exp;
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

    private ManagedObjectReference getSnapshotReference(ManagedObjectReference vmMor, String vmName, String snapshotName) throws Exception {
        System.out.printf("Querying snapshot information for virtual machine (%s)", vmName);
        VirtualMachineSnapshotInfo snapshotInfo = (VirtualMachineSnapshotInfo) getMorProperties(vmMor, new String[] { "snapshot" }).get("snapshot");
        ManagedObjectReference snapshotMor = null;
        String snapshotNotFoundErr = "No snapshot found on virtual machine " + vmName + "with name " + snapshotName;

        if (snapshotInfo != null) {
            List<VirtualMachineSnapshotTree> vmSnapshotList = snapshotInfo.getRootSnapshotList();
            snapshotMor = findSnapshotInTree(vmSnapshotList, snapshotName);
            
            if (snapshotMor == null) {
                System.out.printf("##vso[task.logissue type=error;code=USERINPUT_SnapshotNotFound;TaskId=%s;]", taskId);
                System.err.println(snapshotNotFoundErr);
                throw new Exception();
            }
        } else {
            System.out.printf("##vso[task.logissue type=error;code=USERINPUT_SnapshotNotFound;TaskId=%s;]", taskId);
            System.err.println(snapshotNotFoundErr);
            throw new Exception();
        }
        return snapshotMor;
    }

    private ManagedObjectReference findSnapshotInTree(List<VirtualMachineSnapshotTree> vmSnapshotList, String snapshotName) {
        ManagedObjectReference snapshotMor = null;

        if (vmSnapshotList == null) {
            System.out.printf("Snapshot(%s) not found for virtual machine", snapshotName);
            return snapshotMor;
        }

        for (VirtualMachineSnapshotTree vmSnapshot : vmSnapshotList) {
            if (vmSnapshot.getName().equalsIgnoreCase(snapshotName)) {
                System.out.printf("Found snapshot(%s) for virtual machine", snapshotName);
                return vmSnapshot.getSnapshot();
            } else {
                List<VirtualMachineSnapshotTree> childTree = vmSnapshot.getChildSnapshotList();
                snapshotMor = findSnapshotInTree(childTree, snapshotName);
            }
        }
        return snapshotMor;
    }

    private Map<String, Object> getMorProperties(ManagedObjectReference vmMor, String[] propList) throws Exception {
        PropertyFilterSpec propFilterSpec = createPropFilterSpecForObject(vmMor, propList);
        RetrieveResult results = null;
        try {
            results = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), Arrays.asList(propFilterSpec), new RetrieveOptions());
        } catch(Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_RetriveObjectPropertiesFailed;TaskId=%s;]", taskId);
            System.err.println("Failed to properties for managed object : " + exp.getMessage());
            throw exp;
        }
        final Map<String, Object> propMap = new HashMap<String, Object>();
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

    private Map<String, ManagedObjectReference> getObjectsInContainerByType(ManagedObjectReference container, String morefType) throws Exception {
        PropertyFilterSpec propertyFilterSpecs = createRecursiveFilterSpec(container, morefType, new String [] { "name" });
        
        try {
            System.out.printf("Querying %s objects on vCenter server", morefType);
            RetrieveResult results =  vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), Arrays.asList(propertyFilterSpecs), new RetrieveOptions());
            String token = null;
            final Map<String, ManagedObjectReference> morMap = new HashMap<String, ManagedObjectReference>();
            token = createMap(results, morMap);

            while ( token != null && !token.isEmpty()) {
                results = vimPort.continueRetrievePropertiesEx(serviceContent.getPropertyCollector(), token);
                token = createMap(results, morMap);
            }
            return morMap;
        } catch (Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_QueryObjectsFailed;TaskId=%s;]", taskId);
            System.err.println("Failed to fetch objects: " + exp.getMessage());
            throw exp;
        }
    }

    private String createMap(final RetrieveResult results, final Map<String, ManagedObjectReference> morMap) {
        String token = null;
        
        if(results != null) {
            token = results.getToken();
            String objName = null;
            for (ObjectContent objectContent : results.getObjects()) {
                ManagedObjectReference mor = objectContent.getObj();
                objName = (String) objectContent.getPropSet().get(0).getVal();
                morMap.put(objName, mor);
            }
        }
        
        return token;
    }

    private PropertyFilterSpec createRecursiveFilterSpec(ManagedObjectReference container, String morefType, String[] filterProps) throws Exception {
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, container, Arrays.asList(morefType), true);

            // Create property specification to specify list properties to extract from given object type
            PropertySpec propSpec = new PropertySpec();
            propSpec.setAll(false);
            propSpec.getPathSet().addAll(Arrays.asList(filterProps));
            propSpec.setType(morefType);

            // Create traversal specification to specify inventory navigation path
            TraversalSpec traversalSpc = new TraversalSpec();
            traversalSpc.setName("view");
            traversalSpc.setPath("view");
            traversalSpc.setSkip(false);
            traversalSpc.setType("ContainerView");

            // Create object specification to specify root location and associate a traversal spec
            ObjectSpec objSpec = new ObjectSpec();
            objSpec.setObj(containerView);
            objSpec.setSkip(false);
            objSpec.getSelectSet().add(traversalSpc);
            
            // Create property filter specification, then set property spec and object spec
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propSpec);
            propertyFilterSpec.getObjectSet().add(objSpec);

            return propertyFilterSpec;
        }
        catch(Exception exp) {
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_CreateFilterSpecFailed;TaskId=%s;]", taskId);
            System.err.println("Failed to create filter spec: " + exp.getMessage());
            throw exp;
        }
    }

    private void Init(ConnectionData connData) throws Exception {
        try {
            if(!isSessionActive()) {
                System.out.println("No active session found.. establishing new session.");
                vimService = new VimService();
                vimPort = vimService.getVimPort();

                Map<String, Object> reqContext = ((BindingProvider) vimPort).getRequestContext();
                reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, connData.url.toString());
                reqContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
                ManagedObjectReference serviceInstance = new ManagedObjectReference();
                serviceInstance.setType("ServiceInstance");
                serviceInstance.setValue("ServiceInstance");
                
                serviceContent = vimPort.retrieveServiceContent(serviceInstance);
                rootFolder = serviceContent.getRootFolder();
                userSession = vimPort.login(serviceContent.getSessionManager(), connData.userName,connData.password, null);
            }
        }
        catch (Exception exp) {
            System.err.println("Failed to connect: " + exp.getMessage());
            System.out.printf("##vso[task.logissue type=error;code=PREREQ_ConnectionFailed;TaskId=%s;]", taskId);
            throw exp;
        }
        return;
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
