package banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String fileName = null; // you can give file name here and comment next for-cycle
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-fileName")) {
                fileName = args[i + 1];
            }
        }

        Bank bank = new Bank();
        bank.setFileName(fileName);

        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
        int action = s.nextInt();
        while (action != 0) {
            switch (action) {
                case 1:
                    bank.makeAccount();
                    bank.show();
                    action = s.nextInt();
                    break;
                case 2:
                    if (bank.logIn()) {
                        action = 0;
                    } else {
                        bank.show();
                        action = s.nextInt();
                        break;
                    }
                    break;
                case 3:
                    bank.show();
                    break;
                default:
                    break;
            }
        }
        bank.exit();

    }
}

// class for all operation with bank account also all operation with DB work start here
class Bank {
    Random random = new Random();
    Scanner s = new Scanner(System.in);
    private String fileName;
    private String cardNum; //
    private String pin;     // this fields need for working with current session, when you are make successful logIn
    private int balance = 0;//
    BankDataBase data = new BankDataBase();

    public void setFileName(String fileName) {
        this.fileName = fileName;
        data.setConn(fileName);
    }

    public void makeAccount() {
        String number = getCardNum();
        String tempPin = getPin();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(number);
        System.out.println("Your card PIN:");
        System.out.println(tempPin);
        data.insertNewValues(number, tempPin, 0); // insert in db new values
    }

    private String getCardNum () {
        cardNum = controlSum();
        return cardNum;
    }

    private String getPin() {
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(10);
            num.append(x);
        }
        this.pin = num.toString();
        return  pin;
    } // method to create random pin

    public boolean logIn() { // boolean need to check "exit"
        System.out.println();
        System.out.println("Enter your card number:");
        String tryNum = s.nextLine();
        if (tryNum.equals("")) {
            tryNum = s.nextLine();
        }
        System.out.println("Enter your PIN:");
        String tryPin = s.nextLine();
        if (data.selectNum(tryNum) && data.selectPin(tryNum).equals(tryPin)) { //check contain cardNum in DB and correct pin for this cardNum
            this.cardNum = tryNum;
            this.pin = tryPin;
            this.balance = data.selectBalance(cardNum);
            System.out.println();
            System.out.println("You have successfully logged in!");
            return operaion();
        } else {
            System.out.println("Wrong card number or PIN!");
            return false;
        }
    }

    private boolean operaion () { // this boolean need to check "exit"
        menuShow();
        int action = s.nextInt();
        while (true) {
            switch (action) {
                case 1:
                    System.out.println();
                    System.out.println("Balance: " + data.selectBalance(cardNum));
                    menuShow();
                    action = s.nextInt();
                    break;
                case 2:
                    addIncome();
                    menuShow();
                    action = s.nextInt();
                    break;
                case 3:
                    doTransfer();
                    menuShow();
                    action = s.nextInt();
                    break;
                case 4:
                    closeAccount();
                    return false;
                case 5:
                    System.out.println();
                    this.balance = 0;
                    this.cardNum = null;
                    this.pin = null;
                    System.out.println("You have successfully logged out!");
                    return false;
                default:
                    return true;
            }
        }
    }

    public void show() {
        System.out.println();
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");

    }

    public void menuShow() {
        System.out.println();
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    public void exit(){
        data.close();
        System.out.println();
        System.out.println("Bye!");
    }

    private int  addIncome() {
        System.out.println();
        System.out.println("Enter income:");
        int num = s.nextInt();
        this.balance += num;
        System.out.println("Income was added!"); // don't forget update your inf in DB!
        data.update(cardNum, balance, true);
        return 0;
    }

    private void doTransfer() {
        System.out.println();
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String transferCard = s.next();
        if (!checkLuhn(transferCard)) {
            System.out.println("Probably you made mistake in the card number. Please try again!");
        } else if (transferCard.equals(cardNum)) {
            System.out.println("You can't transfer money to the same account!");
        } else {
            if (!data.selectNum(transferCard)) {
                System.out.println("Such a card does not exist.");
            } else {
                System.out.println("Enter how much money you want to transfer:");
                int transfer = s.nextInt();
                if (transfer > balance) {
                    System.out.println("Not enough money!");
                } else {
                    data.update(transferCard, transfer, true);  // boolean need to math operaton with balance in DB
                    data.update(cardNum, transfer, false);
                    this.balance = data.selectBalance(cardNum);
                    System.out.println("Success!");
                }
            }
        }
    }

    private void closeAccount() {
        data.delete(cardNum);
        this.cardNum = null;
        this.pin = null;
        this.balance = 0;
        System.out.println();
        System.out.println("The account has been closed!");
    }

    private String controlSum () {
        StringBuilder num = new StringBuilder();
        num.append("400000");
        for (int i = 0; i < 9; i++) {
            int x = random.nextInt(10);
            num.append(x);
        }
        String[] num2 = num.toString().split("");
        int sum = 0;
        for (int i = 0; i < 15; i ++) {
            int x = Integer.parseInt(num2[i]);
            if (i % 2 == 0) {
                x = x * 2;
                if (x > 9) {
                    x -= 9;
                }
            }
            sum += x;
        }
        int lastNum = 10 - sum % 10;
        num.append(lastNum % 10);
        return num.toString();
    } // return cardNum

    private boolean checkLuhn (String checkNum) {
        String[] num = checkNum.split("");
        int sum = 0;
        for (int i = 0; i < 16; i++) {
            int x = Integer.parseInt(num[i]);
            if (i % 2 == 0) {
                x = x * 2;
                if (x > 9) {
                    x -= 9;
                }
            }
            sum += x;
        }
        return sum % 10 == 0;
    }
}

class BankDataBase {
    private Connection conn = null;

    public void setConn(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println((e.getMessage()));
        }
        this.conn = con;
        createDataBase(fileName);
        createNewTable(fileName);
    }

    public static void createDataBase (String fileName) {
        String url = "jdbc:sqlite:" + fileName;

        try (Connection con = DriverManager.getConnection(url)) {
            if (con != null) {
                DatabaseMetaData meta = con.getMetaData();
                //System.out.println("The driver name is " + meta.getDriverName());
                //System.out.println("Successfully create new database");
            }
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Create database error");
        }
    }

    public static void createNewTable(String fileName){
        String url = "jdbc:sqlite:" + fileName;

        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "     id INTEGER PRIMARY KEY,\n"
                + "     number TEXT NOT NULL, \n"
                + "     pin TEXT, \n"
                + "     balance INTEGER DEFAULT 0"
                +");";

        try (Connection con = DriverManager.getConnection(url);
             Statement stm = con.createStatement()){
            stm.execute(sql);
            //System.out.println("Successfully create new table in " + fileName);
        }
        catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error with table creator");
        }
    }

    public void insertNewValues(String number, String pin, int balance) {
        String sql = "INSERT INTO card (number, pin, balance) VALUES (?,?,?)";


        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setString(2, pin);
            pstmt.setInt(3, balance);
            pstmt.executeUpdate();
            //System.out.printf("%s and %s successfully added in table\n", number, pin);
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in INSERT");
        }
    }

    public void show() {
        String sql = "SELECT id, number, pin, balance FROM card";
        int i = 0;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-3s %-15s %-6s %-6s\n", rs.getInt("id"), rs.getString("number"), rs.getString("pin"), rs.getInt("balance"));
            }
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in SELECT");
        }
    }

    public void update (String number, int balance, boolean plus) {
        String sql = null;
        if (plus) {
            sql = "UPDATE card SET balance = balance + ? "
                    + " WHERE number = ?";
        } else {
            sql = "UPDATE card SET balance = balance - ? "
                    + " WHERE number = ?";
        }

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, balance);
            pstmt.setString(2, number);

            pstmt.executeUpdate();
            //System.out.println("Complete successfully update");
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error int update");
        }
    }

    public void delete (String number) {
        String sql = "DELETE FROM card WHERE number = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.executeUpdate();
            //System.out.println("Successfully delete data");
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in delete");
        }
    }

    public void deleteAll () {
        String sql = "DELETE FROM card WHERE id > ?";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 0);

            pstmt.executeUpdate();
            System.out.println("Successfully delete data");
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in deleteAll");
        }
    }

    public int selectBalance (String str) {
        String sql = "SELECT id, number, pin, balance FROM card";
        int x = 0;

        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()){
                if (str.equals(rs.getString("number"))){
                    x = rs.getInt("balance");
                }
            }
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in selectBalance");
        }
        return x;
    }

    public String selectPin (String str) {
        String sql = "SELECT id, number, pin, balance FROM card";
        String x = null;

        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()){
                if (str.equals(rs.getString("number"))){
                    x = rs.getString("pin");
                }
            }
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in selectPin");
        }
        return x;
    }

    public boolean selectNum (String str) {
        String sql = "SELECT id, number, pin, balance FROM card";
        str = str.trim();
        boolean ans = false;

        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()){
                if (str.equals(rs.getString("number"))){
                    ans = true;
                }
            }
        } catch (Exception e) {
            System.out.println((e.getMessage()));
            System.out.println("Error in selectNum");
        }

        if (!ans) {
            System.out.println("you check this " + str);
        } else {
            System.out.println(str + " this card exist");
        }
        return ans;
    }

    public void close () {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error in connection close");
        }
    }
}