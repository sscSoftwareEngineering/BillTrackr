/*
 * AUTHOR:  Project Group 1
 * DATE:    11/2018
 * PURPOSE: Access and manipulate MySQL databases for BillTrackr
 */
package billTrackr;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;

public class DataAccess {
    private Connection conn;
    
    DataAccess() {
        //Connect with a preconfigured account
        Properties prop = new Properties();
        final String filename = "config.properties";
        try (InputStream input = DataAccess.class.getResourceAsStream(filename);) {
            if (input != null) {
                prop.load(input);
                System.err.println("Trying to connect to dev server. Remove in final");
                String user = prop.getProperty("dbuser");
                String pass = prop.getProperty("dbpass");
                String db = prop.getProperty("database");
                String hostname = prop.getProperty("dbhostname");
                String port = prop.getProperty("dbport");
                
                conn = getConnection(user, pass, db, hostname, port);
            }
        } catch (IOException e) {
            //Either there is no preconfigured account
            //or the details are wrong.
        }
    }
    
    DataAccess(String user, String pass, String db, String hostname, String port) {
        conn = getConnection(user, pass, db, hostname, port);
    }
    
    public boolean isConnected() {
        return !(conn == null);
    }
    
    public static Connection getConnection(String user, String pass, String db, 
            String hostname, String port) {
        
        String sqlURL = "jdbc:mysql://" + hostname + ":" + port + "/" + db
                + "?useSSL=false";
        try {
            return DriverManager.getConnection(sqlURL, user, pass);
        } catch (SQLException e) {
        }
        return null;
    }

    public Bill getBill(int i) {
        Bill b = null;
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, "
                + "BillDueAmount, BillPaidDate, BillPaidAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID "
                + "WHERE b.BillID = ?;";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                LocalDate billPaidDate = rs.getObject("BillPaidDate", LocalDate.class);
                BigDecimal billPaidAmount = rs.getBigDecimal("BillPaidAmount");
                
                b = new Bill(billID, companyID, billDueDate, billDueAmount, 
                        billPaidDate, billPaidAmount, compName);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        return b;
    }
    
    public ArrayList<Bill> getAllBills() {
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, "
                + "BillDueAmount, BillPaidDate, BillPaidAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID;";
        ArrayList<Bill> bills = new ArrayList();
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                LocalDate billPaidDate = rs.getObject("BillPaidDate", LocalDate.class);
                BigDecimal billPaidAmount = rs.getBigDecimal("BillPaidAmount");
                
                bills.add(new Bill(billID, companyID, billDueDate, 
                        billDueAmount, billPaidDate, billPaidAmount, compName));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return bills;
    }
    
    public ArrayList<Bill> getUnpaidBills() {
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, BillDueAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID "
                + "WHERE b.BillPaidDate IS NULL;";
        ArrayList<Bill> bills = new ArrayList();
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                
                bills.add(new Bill(billID, companyID, billDueDate, billDueAmount, compName));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return bills;
    }
    
    public int updateBill(Bill b) {
        String query = "UPDATE Bills "
                + "SET CompanyID = ?, BillDueDate = ?, BillDueAmount = ?"
                + ", BillPaidDate = ?, BillPaidAmount = ? "
                + "WHERE BillID = ?;";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, b.getCompanyID());
            ps.setObject(2, b.getBillDueDate());
            ps.setBigDecimal(3, b.getBillDueAmount());
            ps.setObject(4, b.getBillPaidDate());
            ps.setBigDecimal(5, b.getBillPaidAmount());
            ps.setInt(6, b.getBillID());
            return ps.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
    
    public int createBill(Bill b) {
        String query = "INSERT INTO Bills "
                + "(CompanyID, BillDueDate, BillDueAmount) "
                + "Values (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, b.getCompanyID());
            ps.setObject(2, b.getBillDueDate());
            ps.setBigDecimal(3, b.getBillDueAmount());
            
            return ps.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
    
    public int deleteBill(Bill b) {
        String query = "DELETE FROM Bills WHERE BillID = ?;";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, b.getBillID());
            
            return ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
}
