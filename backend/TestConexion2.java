import java.sql.*;
public class TestConexion2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com:15029/defaultdb"
            + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&trustServerCertificate=true"
            + "&connectTimeout=180000&socketTimeout=0";
        String pass = System.getProperty("db.pass");
        System.out.println("Intentando conectar (timeout=180s)...");
        long t0 = System.currentTimeMillis();
        try {
            Connection c = DriverManager.getConnection(url, "avnadmin", pass);
            long t1 = System.currentTimeMillis();
            ResultSet r = c.createStatement().executeQuery("SELECT VERSION(), NOW()");
            r.next();
            System.out.println("CONECTADO en " + (t1-t0) + "ms");
            System.out.println("MySQL version: " + r.getString(1));
            System.out.println("Server time:   " + r.getString(2));
            c.close();
        } catch(Exception e) {
            System.out.println("FALLO tras " + (System.currentTimeMillis()-t0) + "ms: " + e.getMessage());
            Throwable c = e.getCause();
            int d = 0;
            while(c != null && d++ < 5) { System.out.println("  [" + d + "] " + c.getMessage()); c = c.getCause(); }
        }
    }
}
