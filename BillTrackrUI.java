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
        
        String mainChoice;
        do {
            println("Please make a selection from the following options");
            println("  1. Pay bills");
            println("  2. Manage all bills");
            println("  3. Manage all companies");
            println("or press ENTER to exit");
            print("> ");
            mainChoice = SCANNER.nextLine();
            switch (mainChoice) {
                case "1"://Pay unpaid bills
                    println("");
                    payBillsUI();
                    break;
                case "2"://Manage all bills
                    println("");
                    manageBillsUI();
                    break;
                case "3"://Manage all companies
                    println("");
                    break;
                case ""://Exit
                    break;
                default:
                    println("That is not a valid option\n");
                    break;
            }
        } while (!mainChoice.equals(""));
        System.exit(0);
    }
    
    private static void payBillsUI() {
        String choice;
        do {
            printUnpaidBills();
            println("Enter the number of a bill you'd like to mark paid");
            println("or press ENTER to exit");
            print("> ");
            choice = SCANNER.nextLine();
            if (!choice.equals("")) {
                int num;
                try {
                    num = Integer.parseInt(choice);
                } catch (NumberFormatException e) {
                    println("That is not a valid option\n");
                    break;
                }
                
                Bill b = getBill(num);
                if(b == null) {
                    println("That is not a valid option\n");
                    break;
                }
                println(Bill.structureUnpaid());
                println(b.toString());
                
                boolean update = false;
                LocalDate paidDate = null;
                BigDecimal paidAmount = null;
                try {
                    print("Enter date paid in YYYY-MM-DD format: ");
                    paidDate = LocalDate.parse(SCANNER.nextLine());
                    print("Enter the amount paid: ");
                    paidAmount = new BigDecimal(SCANNER.nextLine());

                    update = true;
                } catch (DateTimeParseException | NumberFormatException ex) {
                    println("That is not valid input. Please try again.");
                }
                
                if (update) {
                    b.setBillPaidDate(paidDate);
                    b.setBillPaidAmount(paidAmount);

                    println("Updated " + updateBill(b) + " record(s)");
                }
            }
            println("");
        } while (!choice.equals(""));
    }
    
    private static void manageBillsUI() {
        String manageBillsChoice;
        do {
            println("Please make a selection from the following options");
            println("  1. Add a new bill");
            println("  2. Edit an existing bill");
            println("  3. Delete an existing bill");
            println("or press ENTER to exit");
            print("> ");
            manageBillsChoice = SCANNER.nextLine();
            switch (manageBillsChoice) {
                case "1"://Add a new bill
                    println("");
                    try {
                        print("Enter company ID number: ");
                        int companyID = Integer.parseInt(SCANNER.nextLine());
                        print("Enter due date in YYYY-MM-DD format: ");
                        LocalDate dueDate = LocalDate.parse(SCANNER.nextLine());
                        print("Enter amount due: ");
                        BigDecimal dueAmount = new BigDecimal(SCANNER.nextLine());
                        
                        Bill b = new Bill(companyID, dueDate, dueAmount);
                        
                        println("Inserted " + createBill(b) + " record(s)");
                    } catch (DateTimeParseException | NumberFormatException e) {
                        println("That is not valid input. Please try again.");
                    }
                    break;
                case "2"://Edit an existing bill
                    println("");
                    printBills();
                    String editChoice;
                    do {
                        println("");
                        println("Enter the number of a bill you'd like to edit");
                        println("or press ENTER to exit");
                        print("> ");
                        editChoice = SCANNER.nextLine();
                        try {
                            int i = Integer.parseInt(editChoice);
                            Bill b = getBill(i);
                            if (b == null) {
                                println("That id does not exist.");
                            } else {
                                editBillsUI(b);
                            }
                        } catch (NumberFormatException ex) {
                            println("That is not a valid option");
                        }
                    } while (!editChoice.equals(""));
                    break;

                case "3"://Delete an existing bill
                    println("");
                    printBills();
                    String deleteChoice;
                    do {
                        println("");
                        println("Enter the number of a bill you'd like to delete");
                        println("or press ENTER to exit");
                        print("> ");
                        deleteChoice = SCANNER.nextLine();
                        try {
                            int i = Integer.parseInt(deleteChoice);
                            Bill b = getBill(i);
                            if (b == null) {
                                println("That id does not exist.");
                            } else {
                                deleteBill(b);
                            }
                        } catch (NumberFormatException ex) {
                            println("That is not a valid option");
                        }
                    } while (!deleteChoice.equals(""));
                    break;
                case ""://Exit
                    break;
                default:
                    println("That is not a valid option");
                    break;
            }
            println("");
        } while (!manageBillsChoice.equals(""));
    }
    
    private static void editBillsUI(Bill b) {
        println(Bill.structurePaid());
        println(b.toString());

        try {
            String input;
            print("Enter company ID[" + b.getCompanyID() + "]: ");
            input = SCANNER.nextLine();
            if (!input.equals("")){
                b.setCompanyID(Integer.parseInt(input));
            }

            print("Enter due date[" + b.getBillDueDate()+ "]: ");
            input = SCANNER.nextLine();
            if (!input.equals("")){
                b.setBillDueDate(LocalDate.parse(input));
            }

            print("Enter amount due[" + b.getBillDueAmount()+ "]: ");
            input = SCANNER.nextLine();
            if (!input.equals("")){
                b.setBillDueAmount(new BigDecimal(input));
            }

            boolean unpaid = b.getBillPaidDate() == null;
            int edit = -1;
            do {
                if (unpaid) {
                    print("This bill is not paid. Would you like to leave it unpaid?[Yes]: ");
                } else {
                    print("This bill is marked paid. Would you like to mark it unpaid?[Yes]: ");
                }
                input = SCANNER.nextLine().toLowerCase();
                switch (input) {
                    case "":
                    case "y":
                    case "yes":
                        edit = 0;
                        break;
                    case "n":
                    case "no":
                        edit = 1;
                        break;
                    default:
                        println("Enter Yes or No");
                        break;
                }
            // while string is not a valid option
            } while (edit == -1);

            if (edit == 0) {
                b.setBillPaidDate(null);
                b.setBillPaidAmount(null);

            } else if (edit == 1) {
                print("Enter date paid[" + b.getBillPaidDate()+ "]: ");
                input = SCANNER.nextLine();
                if (!input.equals("")){
                    b.setBillPaidDate(LocalDate.parse(input));
                }

                if (b.getBillPaidDate() == null) {
                    //If paid date still null
                    println("No paid date given. Keeping bill unpaid");
                    b.setBillPaidAmount(null);//Cleanup
                } else {
                    print("Enter amount paid[" + b.getBillPaidAmount()+ "]: ");
                    input = SCANNER.nextLine();
                    if (!input.equals("")){
                        b.setBillPaidAmount(new BigDecimal(input));
                    } else {
                        if (b.getBillPaidAmount() == null) {
                            println("A paid amount is required to mark bill paid. Marking bill unpaid.");
                            b.setBillPaidDate(null);
                        }
                    }
                }
            }

            println("Updated " + updateBill(b) + " record(s)");
        } catch (DateTimeParseException | NumberFormatException ex) {
            println("That is not valid input. Please try again.");
        }

    }
    
    private static void printBills() {
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, "
                + "BillDueAmount, BillPaidDate, BillPaidAmount "
                + "FROM Bills b JOIN Companies c ON c.CompanyID = b.CompanyID;";
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ResultSet rs = ps.executeQuery();
            
            ArrayList<Bill> bills = new ArrayList();
            
            println(Bill.structurePaid());
            while(rs.next()) {
                int billID = rs.getInt("BillID");
                int companyID = rs.getInt("CompanyID");
                String compName = rs.getString("CompName");
                LocalDate billDueDate = rs.getObject("BillDueDate", LocalDate.class);
                BigDecimal billDueAmount = rs.getBigDecimal("BillDueAmount");
                LocalDate billPaidDate = rs.getObject("BillPaidDate", LocalDate.class);
                BigDecimal billPaidAmount = rs.getBigDecimal("BillPaidAmount");
                
                bills.add(new Bill(billID, companyID, billDueDate, billDueAmount, billPaidDate, billPaidAmount, compName));
            }
            bills.forEach((Bill b) -> {
                println(b.toString());
            });
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
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
            bills.forEach((Bill b) -> {
                println(b.toString());
            });
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
    }
    
    private static Bill getBill(int i) {
        Bill b = null;
        String query = "SELECT BillID, b.CompanyID, CompName, BillDueDate, "
                + "BillDueAmount, BillPaidDate, BillPaidAmount "
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
                LocalDate billPaidDate = rs.getObject("BillPaidDate", LocalDate.class);
                BigDecimal billPaidAmount = rs.getBigDecimal("BillPaidAmount");
                
                b = new Bill(billID, companyID, billDueDate, billDueAmount, 
                        billPaidDate, billPaidAmount, compName);
            }
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
        
        return b;
    }
    
    private static int updateBill(Bill b) {
        String query = "UPDATE Bills "
                + "SET CompanyID = ?, BillDueDate = ?, BillDueAmount = ?"
                + ", BillPaidDate = ?, BillPaidAmount = ? "
                + "WHERE BillID = ?;";
        
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, b.getCompanyID());
            ps.setObject(2, b.getBillDueDate());
            ps.setBigDecimal(3, b.getBillDueAmount());
            ps.setObject(4, b.getBillPaidDate());
            ps.setBigDecimal(5, b.getBillPaidAmount());
            ps.setInt(6, b.getBillID());
            return ps.executeUpdate();
            
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
        return 0;
    }
    
    private static int createBill(Bill b) {
        String query = "INSERT INTO Bills "
                + "(CompanyID, BillDueDate, BillDueAmount) "
                + "Values (?, ?, ?)";
        
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, b.getCompanyID());
            ps.setObject(2, b.getBillDueDate());
            ps.setBigDecimal(3, b.getBillDueAmount());
            
            return ps.executeUpdate();
            
        } catch (SQLException ex) {
            println(ex.getMessage());
        }
        return 0;
    }
    
    private static int deleteBill(Bill b) {
        String query = "DELETE FROM Bills WHERE BillID = ?;";
        try (
                Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, b.getBillID());
            
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
