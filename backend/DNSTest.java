import java.net.*;
public class DNSTest {
    public static void main(String[] args) throws Exception {
        String host = "mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com";
        InetAddress[] addrs = InetAddress.getAllByName(host);
        System.out.println("Java DNS - " + host + ":");
        for (InetAddress a : addrs) System.out.println("  -> " + a);
        
        // Test raw socket
        System.out.println("\nTest socket directo (5s timeout):");
        long t0 = System.currentTimeMillis();
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, 15029), 5000);
            s.setSoTimeout(5000);
            byte[] buf = new byte[10];
            int n = s.getInputStream().read(buf);
            System.out.println("Conectado en " + (System.currentTimeMillis()-t0) + "ms, " + n + " bytes: primer=" + buf[0]);
        } catch(Exception e) {
            System.out.println("FALLO socket: " + e.getMessage());
        }
    }
}
