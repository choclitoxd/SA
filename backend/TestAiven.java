import java.sql.*;
public class TestAiven {
    public static void main(String[] args) throws Exception {
        String host = "mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com";
        int port = 15029;
        String db = "defaultdb";
        String user = "avnadmin";
        String pass = System.getProperty("db.pass");
        
        String[] urls = {
            "jdbc:mysql://" + host + ":" + port + "/" + db + "?sslMode=REQUIRED",
            "jdbc:mysql://" + host + ":" + port + "/" + db + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true",
            "jdbc:mysql://" + host + ":" + port + "/" + db,
        };
        
        for (String url : urls) {
            String label = url.contains("?") ? url.substring(url.indexOf("?")+1) : "(sin params)";
            System.out.println("\n--- " + label);
            long t0 = System.currentTimeMillis();
            try {
                Connection c = DriverManager.getConnection(url, user, pass);
                System.out.println("OK en " + (System.currentTimeMillis()-t0) + "ms");
                ResultSet r = c.createStatement().executeQuery("SELECT VERSION()");
                r.next(); System.out.println("MySQL: " + r.getString(1));
                c.close(); break;
            } catch(Exception e) {
                System.out.println("FALLO en " + (System.currentTimeMillis()-t0) + "ms");
                System.out.println("  " + e.getMessage());
                for(Throwable t = e.getCause(); t != null; t = t.getCause())
                    System.out.println("  -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }
}
