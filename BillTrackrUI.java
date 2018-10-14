/*
 * AUTHOR:  Project Group 1
 * DATE:    10/2018
 * PURPOSE: Main UI for BillTrackr
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class BillTrackrUI {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static String username;
    private static String password;
    private static String db = "BillTrackr";
    private static String hostname = "localhost";
    private static String port = "3306";
    
    public static void main(String[] args) {
        
        println("Welcome to BillTrackr");
        // TODO Remove dev server in production
        Properties prop = new Properties();
        String filename = "config.properties";
        try (InputStream input = BillTrackrUI.class.getResourceAsStream(filename);) {
            if (input != null) {
                prop.load(input);
                System.err.println("Trying to connect to dev server. Remove in final");
                username = prop.getProperty("dbuser");
                password = prop.getProperty("dbpass");
                db = prop.getProperty("database");
                hostname = prop.getProperty("dbhostname");
                port = prop.getProperty("dbport");
                Connection temp = connect();
                println("Welcome dev");
            } else {
                throw new IOException();
            }
        } catch (IOException | SQLException e) {
            String input;
            
            println("Please enter your database details");
            print("Username: ");
            username = SCANNER.nextLine();
            
            print("Password: ");
            password = SCANNER.nextLine();
            
            print("Database[" + db + "]: ");
            input = SCANNER.nextLine();
            db = input.equals("") ? db : input;
            
            print("Hostname[" + hostname + "]: ");
            input = SCANNER.nextLine();
            hostname = input.equals("") ? hostname : input;
            
            print("Port[" + port + "]: ");
            input = SCANNER.nextLine();
            port = input.equals("") ? port : input;

            try (Connection temp = connect()) {
                println("Connected");
            }
            catch (SQLException ex) {
                println("Connection Failed");
                println("Press ENTER to exit.");
                SCANNER.nextLine();
                System.exit(0);
            }
        }
        
        String choice;
        do {
            println("Please make a selection from the following options");
            println("  1. Pay bills");
            println("  2. Manage all bills");
            println("  3. Manage all companies");
            println("  0. Exit");
            print("> ");
            choice = SCANNER.nextLine();
            switch (choice) {
                case "0":
                    //Exit
                    break;
                case "1"://Pay unpaid bills
                    println("");
                    payBills();
                    break;
                case "2"://Manage all bills
                    println("");
                    break;
                case "3"://Manage all companies
                    println("");
                    break;
                default:
                    println("That is not a valid option");
                    println("");
                    break;
            }
        } while (!choice.equals("0"));
        System.exit(0);
    }
    
    private static void payBills() {
        String choice;
        do {
            printUnpaidBills();
            println("Enter the number of a bill you'd like to mark paid");
            println("or press ENTER to exit");
            print("> ");
            choice = SCANNER.nextLine();
            if (!choice.equals("")) {
                try{
                    int i = Integer.parseInt(choice);
                    Bill b = getBill(i);
                    if(b == null) {
                        println("That is not a valid option");
                        continue;
                    }
                    println(Bill.structureUnpaid());
                    println(b.toStringUnpaid());
                    
                    try {
                        print("Enter date paid in YYYY-MM-DD format: ");
                        LocalDate paidDate = LocalDate.parse(SCANNER.nextLine());
                        print("Enter the amount paid: ");
                        BigDecimal paidAmount = new BigDecimal(SCANNER.nextLine());
                        
                        b.setBillPaidDate(paidDate);
                        b.setBillPaidAmount(paidAmount);
                        
                        println("Updated " + updateBill(b) + " record(s)");
                    } catch (DateTimeParseException | NumberFormatException ex) {
                        println("That is not valid input. Please try again.");
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    println("That is not a valid option");
                }
            }
            println("");
        } while (!choice.equals(""));
    }
    
    private static void printUnpaidBills() {
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, BillDueAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID "
                + "WHERE b.BillPaidDate IS NULL;";
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ResultSet rs = ps.executeQuery();
            
            ArrayList<Bill> bills = new ArrayList();
            
            println(Bill.structureUnpaid());
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                
                bills.add(new Bill(billID, companyID, billDueDate, billDueAmount, compName));
            }
            bills.forEach((b) -> {
                println(b.toStringUnpaid());
            });
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
    }
    
    private static Bill getBill(int i) {
        Bill b = null;
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, BillDueAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID "
                + "WHERE b.BillID = ?;";
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, i);
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                
                b = new Bill(billID, companyID, billDueDate, billDueAmount, compName);
            }
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
        
        return b;
    }
    
    private static int updateBill(Bill b) {
        boolean paid = true;
        if (b.getBillPaidAmount() == null || b.getBillPaidDate() == null) {
            paid = false;
        }
        
        String query = "UPDATE Bills "
                + "SET CompanyID = ?, BillDueDate = ?, BillDueAmount = ?";
        if (paid) {
            query += ", BillPaidDate = ?, BillPaidAmount = ?";
        }
        query += " WHERE BillID = ?;";
        
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, b.getCompanyID());
            ps.setObject(2, b.getBillDueDate());
            ps.setBigDecimal(3, b.getBillDueAmount());
            if(paid) {
                ps.setObject(4, b.getBillPaidDate());
                ps.setBigDecimal(5, b.getBillPaidAmount());
                ps.setInt(6, b.getBillID());
            } else {
                ps.setInt(4, b.getBillID());
            }
            
            //ParameterMetaData d = ps.getParameterMetaData();// TODO Remove
            
            return ps.executeUpdate();
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
        return 0;
    }
    
    private static Connection connect() throws SQLException {
        String sqlURL = "jdbc:mysql://" + hostname + ":" + port + "/" + db
                + "?useSSL=false";
        
        return DriverManager.getConnection(sqlURL, username, password);
    }
    
    private static void println(String text) {
        System.out.println(text);
    }
    
    private static void print(String text) {
        System.out.print(text);
    } 
}
