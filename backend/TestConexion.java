import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TestConexion {
    public static void main(String[] args) throws Exception {
        String[] urls = {
            "jdbc:mysql://mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com:15029/defaultdb?sslMode=REQUIRED&allowPublicKeyRetrieval=true&tlsVersions=TLSv1.2&trustServerCertificate=true&connectTimeout=60000",
            "jdbc:mysql://mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com:15029/defaultdb?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=60000",
            "jdbc:mysql://mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com:15029/defaultdb?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=60000"
        };

        String user = "avnadmin";
        String pass = System.getProperty("db.pass");

        for (String url : urls) {
            System.out.println("\n--- Probando: " + url.substring(url.indexOf('?')+1, Math.min(url.indexOf('?')+50, url.length())) + "...");
            try {
                long t0 = System.currentTimeMillis();
                Connection conn = DriverManager.getConnection(url, user, pass);
                long t1 = System.currentTimeMillis();
                ResultSet rs = conn.createStatement().executeQuery("SELECT VERSION()");
                rs.next();
                System.out.println("CONECTADO en " + (t1-t0) + "ms — MySQL " + rs.getString(1));
                conn.close();
            } catch(Exception e) {
                System.out.println("FALLO: " + e.getMessage());
                Throwable c = e.getCause();
                while(c != null) { System.out.println("  -> " + c.getMessage()); c = c.getCause(); }
            }
        }
    }
}
