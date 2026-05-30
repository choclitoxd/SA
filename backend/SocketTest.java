import java.net.*;
import java.io.*;
public class SocketTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Conectando...");
        Socket s = new Socket();
        s.connect(new InetSocketAddress("mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com", 15029), 10000);
        s.setSoTimeout(10000);
        InputStream in = s.getInputStream();
        
        // Leer el greeting completo
        byte[] buf = new byte[512];
        int total = 0;
        long t0 = System.currentTimeMillis();
        try {
            while (total < buf.length) {
                int n = in.read(buf, total, buf.length - total);
                if (n < 0) break;
                total += n;
                if (System.currentTimeMillis() - t0 > 2000) break;
            }
        } catch(SocketTimeoutException e) { /* expected */ }
        
        System.out.println("Bytes recibidos: " + total);
        // Mostrar los primeros bytes como texto (server version está en el greeting)
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < Math.min(total, 100); i++) {
            if(buf[i] >= 32 && buf[i] < 127) sb.append((char)buf[i]); 
            else sb.append('[').append(buf[i]&0xFF).append(']');
        }
        System.out.println("Contenido: " + sb);
        s.close();
    }
}
