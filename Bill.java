/*
 * AUTHOR:  Project Group 1
 * DATE:    10/2018
 * PURPOSE: This class decribes the bills used in BillTrackr
 */
package billTrackr;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Bill {
    private int billID;
    private int companyID;
    private LocalDate billDueDate;
    private BigDecimal billDueAmount;
    private LocalDate billPaidDate = null;
    private BigDecimal billPaidAmount = null;
    private String compName = "";
    
    //Constructors
    /**
     * Create an unpaid bill to send to the database
     * @param billID
     * @param companyID
     * @param billDueDate
     * @param billDueAmount 
     */
    Bill(int billID, int companyID, LocalDate billDueDate, BigDecimal billDueAmount) {
        this(billID, companyID, billDueDate, billDueAmount, "");
    }
    /**
     * Create an unpaid bill from the database including the name
     * @param billID
     * @param companyID
     * @param billDueDate
     * @param billDueAmount
     * @param compName 
     */
    Bill(int billID, int companyID, LocalDate billDueDate, BigDecimal billDueAmount, 
            String compName) {
        this(billID, companyID, billDueDate, billDueAmount, null, null, compName);
    }
    /**
     * Create a paid bill to send to the database
     * @param billID
     * @param companyID
     * @param billDueDate
     * @param billDueAmount
     * @param billPaidDate
     * @param billPaidAmount 
     */
    Bill(int billID, int companyID, LocalDate billDueDate, BigDecimal billDueAmount, 
            LocalDate billPaidDate, BigDecimal billPaidAmount) {
        this(billID, companyID, billDueDate, billDueAmount, billPaidDate, billPaidAmount, "");
    }
    /**
     * Create a paid bill from the database including the name
     * @param billID
     * @param companyID
     * @param billDueDate
     * @param billDueAmount
     * @param billPaidDate
     * @param billPaidAmount
     * @param compName 
     */
    Bill(int billID, int companyID, LocalDate billDueDate, BigDecimal billDueAmount, 
            LocalDate billPaidDate, BigDecimal billPaidAmount, String compName) {
        this.billID = billID;
        this.companyID = companyID;
        this.billDueDate = billDueDate;
        this.billDueAmount = billDueAmount;
        this.billPaidDate = billPaidDate;
        this.billPaidAmount = billPaidAmount;
        this.compName = compName;
    }

    //Accessor methods
    public int getBillID() {
        return billID;
    }
    public int getCompanyID() {
        return companyID;
    }
    public void setCompanyID(int companyID) {
        if (companyID > 0) {
            this.companyID = companyID;
        }
    }
    public LocalDate getBillDueDate() {
        return billDueDate;
    }
    public void setBillDueDate(LocalDate billDueDate) {
        this.billDueDate = billDueDate;
    }
    public BigDecimal getBillDueAmount() {
        return billDueAmount;
    }
    public void setBillDueAmount(BigDecimal billDueAmount) {
        if (billDueAmount.compareTo(BigDecimal.ZERO) == 1) {//Greater than 0
            this.billDueAmount = billDueAmount;
        }
    }
    public LocalDate getBillPaidDate() {
        return billPaidDate;
    }
    public void setBillPaidDate(LocalDate billPaidDate) {
        this.billPaidDate = billPaidDate;
    }
    public BigDecimal getBillPaidAmount() {
        return billPaidAmount;
    }
    public void setBillPaidAmount(BigDecimal billPaidAmount) {
        if (billPaidAmount.compareTo(BigDecimal.ZERO) == 1) {//Greater than 0
            this.billPaidAmount = billPaidAmount;
        }
    }
    public String getCompName() {
        return compName;
    }
    
    public String toStringUnpaid() {
        return String.format("%d, %d, \"%s\", %s, $%.2f", 
                billID, companyID, compName, billDueDate, billDueAmount);
    }
    
    public String toStringPaid() {
        return String.format("%d, %d, \"%s\", %s, $%.2f%n, %s, $%.2f", 
                billID, companyID, compName, billDueDate, billDueAmount, 
                billPaidDate, billPaidAmount);
    }
    
    public static String structureUnpaid() {
        return "billID, companyID, compName, billDueDate, billDueAmount";
    }
    
    public static String structurePaid() {
        return "billID, companyID, compName, billDueDate, billDueAmount, "
                + "billPaidDate, billPaidAmount";
    }
}
