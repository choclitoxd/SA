import java.sql.*;
public class TestMariaDB {
    public static void main(String[] args) throws Exception {
        String host = "mysql-291cc8ba-uqvirtual-cb32.d.aivencloud.com";
        String pass = System.getProperty("db.pass");
        String[] urls = {
            "jdbc:mariadb://" + host + ":15029/defaultdb?sslMode=REQUIRED",
            "jdbc:mariadb://" + host + ":15029/defaultdb?sslMode=DISABLE",
            "jdbc:mariadb://" + host + ":15029/defaultdb",
        };
        for (String url : urls) {
            String label = url.contains("?") ? url.substring(url.indexOf("?")+1) : "(sin params)";
            System.out.println("\n--- mariadb: " + label);
            long t0 = System.currentTimeMillis();
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                Connection c = DriverManager.getConnection(url, "avnadmin", pass);
                System.out.println("CONECTADO en " + (System.currentTimeMillis()-t0) + "ms");
                ResultSet r = c.createStatement().executeQuery("SELECT VERSION()");
                r.next(); System.out.println("MySQL: " + r.getString(1));
                c.close(); return;
            } catch(Exception e) {
                System.out.println("FALLO en " + (System.currentTimeMillis()-t0) + "ms: " + e.getMessage());
                for(Throwable t = e.getCause(); t!=null; t = t.getCause())
                    System.out.println("  -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }
}
