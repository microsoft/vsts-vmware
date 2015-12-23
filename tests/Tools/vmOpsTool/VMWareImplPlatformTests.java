public class VMWareImplPlatformTests extends VMWarePlatformTests {

    @Override
    public IVMWare getVmWareImpl() {
        return new VMWareImpl();
    }

    @Override
    public String getvCenterUrl() {
        return System.getenv("VCENTER_URL") + "/sdk/vimservice";
    }
}