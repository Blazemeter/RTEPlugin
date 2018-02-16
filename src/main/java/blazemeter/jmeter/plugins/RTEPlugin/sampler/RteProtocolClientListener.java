import java.util.List;

public interface RteProtocolClientListener {
    public void Connect(String server, int port, String username, String password, SecurityProtocol protocol);

    public String send(List<Coordinput> input, List<RteWaiter> waiters);

    public void disconnect();
}
