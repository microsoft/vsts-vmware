import vmOpsTool.IVMWare;
import vmOpsTool.VMWareImpl;

public class VMWareImplPlatformTests extends VMWarePlatformTests {

    @Override
    public IVMWare getVmWareImpl() {
        return new VMWareImpl();
    }

    @Override
    public String getvCenterUrl() {
        return "https://idcvstt-lab325.fareast.corp.microsoft.com/sdk/vimservice";
    }
}