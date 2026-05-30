import java.sql.*;
public class TestOriginal {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com:15029/defaultdb"
            + "?useSSL=true&allowPublicKeyRetrieval=true&enabledTLSProtocols=TLSv1.2,TLSv1.3"
            + "&connectTimeout=60000&socketTimeout=60000";
        System.out.println("URL original - intentando...");
        long t0 = System.currentTimeMillis();
        try {
            Connection c = DriverManager.getConnection(url, "avnadmin", System.getProperty("db.pass"));
            System.out.println("CONECTADO en " + (System.currentTimeMillis()-t0) + "ms");
            ResultSet r = c.createStatement().executeQuery("SELECT VERSION(), NOW()");
            r.next();
            System.out.println("Version: " + r.getString(1) + " | Time: " + r.getString(2));
            c.close();
        } catch(Exception e) {
            System.out.println("FALLO en " + (System.currentTimeMillis()-t0) + "ms: " + e.getMessage());
            for(Throwable t = e.getCause(); t != null; t = t.getCause())
                System.out.println("  -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }
}
