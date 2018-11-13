/*
 * AUTHOR:  Project Group 1
 * DATE:    10/2018
 * PURPOSE: Main UI for BillTrackr
 */
package billTrackr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class BillTrackrUI {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static DataAccess da;
    
    public static void main(String[] args) {
        
        println("Welcome to BillTrackr");
        da = new DataAccess();
        if (da.isConnected()) {
            println("Welcome dev");
        } else {
            String input;
            
            println("Please enter your database details");
            print("Username: ");
            String username = SCANNER.nextLine();
            
            print("Password: ");
            String password = SCANNER.nextLine();
            
            print("Database: ");
            input = SCANNER.nextLine();
            String db = input.equals("") ? "BillTrackr" : input;
            
            print("Hostname[localhost]: ");
            input = SCANNER.nextLine();
            String hostname = input.equals("") ? "localhost" : input;
            
            print("Port[port]: ");
            input = SCANNER.nextLine();
            String port = input.equals("") ? "3306" : input;
            
            da = new DataAccess(username, password, db, hostname, port);

            if (da.isConnected()) {
                println("Connected");
            } else {
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
        String payBillsChoice;
        do {
            println(Bill.structureUnpaid());
            da.getUnpaidBills().forEach((b) -> {
                println(b.toString());
            });
            
            println("Enter the number of a bill you'd like to mark paid");
            println("or press ENTER to exit");
            print("> ");
            payBillsChoice = SCANNER.nextLine();
            if (!payBillsChoice.equals("")) {
                int num;
                try {
                    num = Integer.parseInt(payBillsChoice);
                } catch (NumberFormatException e) {
                    println("That id does not exist\n");
                    break;
                }
                
                Bill b = da.getBill(num);
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

                    println("Updated " + da.updateBill(b) + " record(s)");
                }
            }
            println("");
        } while (!payBillsChoice.equals(""));
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
                    try {
                        print("\nEnter company ID number: ");
                        int companyID = Integer.parseInt(SCANNER.nextLine());
                        print("Enter due date in YYYY-MM-DD format: ");
                        LocalDate dueDate = LocalDate.parse(SCANNER.nextLine());
                        print("Enter amount due: ");
                        BigDecimal dueAmount = new BigDecimal(SCANNER.nextLine());
                        
                        Bill b = new Bill(companyID, dueDate, dueAmount);
                        
                        println("Inserted " + da.createBill(b) + " record(s)");
                    } catch (DateTimeParseException | NumberFormatException e) {
                        println("That is not valid input. Please try again.");
                    }
                    break;
                case "2"://Edit an existing bill
                    println("\n" + Bill.structurePaid());
                    da.getAllBills().forEach((b) -> {
                        println(b.toString());
                    });
                    
                    String editChoice;
                    do {
                        println("\nEnter the number of a bill you'd like to edit");
                        println("or press ENTER to exit");
                        print("> ");
                        editChoice = SCANNER.nextLine();
                        try {
                            int i = Integer.parseInt(editChoice);
                            Bill b = da.getBill(i);
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
                    println("\n" + Bill.structurePaid());
                    da.getAllBills().forEach((b) -> {
                        println(b.toString());
                    });
                    
                    String deleteChoice;
                    do {
                        println("\nEnter the number of a bill you'd like to delete");
                        println("or press ENTER to exit");
                        print("> ");
                        deleteChoice = SCANNER.nextLine();
                        try {
                            int i = Integer.parseInt(deleteChoice);
                            Bill b = da.getBill(i);
                            if (b == null) {
                                println("That id does not exist.");
                            } else {
                                da.deleteBill(b);
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

            println("Updated " + da.updateBill(b) + " record(s)");
        } catch (DateTimeParseException | NumberFormatException ex) {
            println("That is not valid input. Please try again.");
        }

    }
    
    private static void println(String text) {
        System.out.println(text);
    }
    
    private static void print(String text) {
        System.out.print(text);
    } 
}
