package com.example.ammarhasan.budgeter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ammar Hasan  150454388 April 2018
 * Class Purpose: this class represents a user object
 * which will be used to store user information
 */
public class User {

    private double bankAmount;
    private List<Budget> budgets; // list of budgets set by user
    private List<Transaction> transactions; // list of transactions made by user
    private double projectedSpend;
    private double targetSaving;
    private double lowBankWarning;

    private static final String NOT_ENOUGH_BANK_BUD = "Not enough bank for budget";
    private static final String NOT_ENOUGH_BUD_TRANS = "Not enough budget for transaction";
    private static final String NAME_USED = "Name was used";
    private static final String LESS_ZERO = "Must be larger than zero";
    private static final double DEF_SAVING_TAR = 150;
    private static final double DEF_LOW_BANK_WARN = 80;

    /**
     * User default constructor
     */
    public User() {
        bankAmount = 0.0;
        projectedSpend = 0.0;
        targetSaving = DEF_SAVING_TAR;
        lowBankWarning = DEF_LOW_BANK_WARN;
        budgets = new ArrayList<>();
        transactions = new ArrayList<>();
    }


    /**
     * Finds amount spend this day
     * @return Amount spent today as a double
     */
    public double getDaySpend(){
        double total = 0;

        // for all transactions, check if they occur today and are debit to add to total
        for (Transaction t : transactions) {
            if(t.getDateTime().getDay() == ZonedDateTime.now().getDayOfMonth()
                    && !t.getCredit()){
                total = t.getAmount() + total;
            }
        }
        return total;
    }

    /**
     * Finds amount spend this month
     * @return Amount spent this month as a double
     */
    public double getMonthSpend(){
        double total = 0;

        // for all transactions, check if they occured this month and are debit to add to total
        for (Transaction t : transactions) {
            if(t.getDateTime().getMonth() == ZonedDateTime.now().getMonthValue()
                    && !t.getCredit()){
                total = t.getAmount() + total;
            }
        }
        return total;
    }

    /**
     * Finds amount spend this year
     * @return Amount spent this year as a double
     */
    public double getYearSpend(){
        double total = 0;

        // for all transactions, check if they occured this year and are debit to add to total
        for (Transaction t : transactions) {
            if(t.getDateTime().getYear() == ZonedDateTime.now().getYear()
                    && !t.getCredit()){
                total = t.getAmount() + total;
            }
        }
        return total;
    }

    /**
     * Finds amount earned this year
     * @return Amount earned this year as a double
     */
    public double getYearEarn(){
        double total = 0;

        // for all transactions, check if they occured this year and are credit to add to total
        for (Transaction t : transactions) {
            if(t.getDateTime().getYear() == ZonedDateTime.now().getYear()
                    && t.getCredit()){
                total = t.getAmount() + total;
            }
        }
        return total;
    }

    /**
     * Finds a given budget
     * @param budgetName Name of budget to find
     * @return Budget as a Budget object or null if not found
     */
    public Budget findBudget(String budgetName){
        for(Budget b : budgets){
            if(b.getName().equals(budgetName)){
                return b;
            }
        }
        return null; // not found
    }

    /**
     * Carries a transaction to a budget
     * @param budget Budget to carry transaction with
     * @param amount Amount to spend
     * @param credit Is it credit (true) or debit (false)
     */
    public void carryTransaction(Budget budget, double amount, boolean credit){

        // error checking
        if(amount <= 0){ // can't have 0 or less amount
            throw new IllegalArgumentException(LESS_ZERO);
        }

        // Check if enough bank in budget exists
        if(!credit && (budget.getRemaining() - amount) < 0){
            throw new IllegalArgumentException(NOT_ENOUGH_BUD_TRANS);
        }

        // create a new transaction
        Transaction transaction =
               // new Transaction(ZonedDateTime.now(), credit, amount, budget);
                new Transaction(credit, amount, budget);
        transactions.add(transaction);

        // if debit, amount of project spend and budget will fall.
        if(!credit){
            projectedSpend = projectedSpend - amount;
            budget.setRemaining(budget.getRemaining() - amount);
            bankAmount = bankAmount - amount;
        }else { // else just add to bank amount
            bankAmount = bankAmount + amount;
        }

    }

    /**
     * Resets budget remaining amount
     * @param budgetName Name of budget to reset
     * @throws IllegalArgumentException if not enough bank exists to reset the budget
     */
    public void resetBudget(String budgetName) throws IllegalArgumentException{

        // find budget to update
        Budget budget = findBudget(budgetName);

        if(budget != null) {

            // error check, see if enough budget exists to reset allocated amount
            if ((bankAmount - (projectedSpend + (budget.getAllocated() - budget.getRemaining()))) < 0) {
                throw new IllegalArgumentException(NOT_ENOUGH_BANK_BUD);
            }

            // update projected spending (difference between allocated and remaining)
            projectedSpend = projectedSpend + (budget.getAllocated() - budget.getRemaining());

            // reset remaining
            budget.setRemaining(budget.getAllocated());
        }
    }

    /**
     * Adds a budget to user budgets
     * @param budget user new budget
     * @throws IllegalArgumentException if name is used or not enough bank exists
     */
    public void addBudget(Budget budget) throws IllegalArgumentException{

        // error checking

        // See if enough bank exists for budget
        if(bankAmount - (budget.getRemaining() + projectedSpend) < 0){
            throw new IllegalArgumentException(NOT_ENOUGH_BANK_BUD);
        }

        // error checking
        if(budget.getAllocated() <= 0){ // can't have 0 or less amount
            throw new IllegalArgumentException(LESS_ZERO);
        }

        for (Budget b: budgets) {
            if(b.getName().equals(budget.getName())){
                throw new IllegalArgumentException(NAME_USED);
            }
        }

        budgets.add(budget);
        projectedSpend = projectedSpend + budget.getAllocated();
    }

    /**
     * Updates a given budget by budgetName
     * @param budgetName budget name to update
     * @param newBudgetName new budget name
     * @param newAllocated new allocated amount
     * @throws IllegalArgumentException thrown when not enough bank exists or name is used
     */
    public void updateBudget(String budgetName, String newBudgetName,
                             double newAllocated) throws IllegalArgumentException{

        // find budget
        Budget budget = findBudget(budgetName);

        // find difference in allocated amount
        double allocatedDifference = newAllocated - budget.getAllocated();

        // error checking

        if ((bankAmount - (allocatedDifference + projectedSpend)) < 0) {
            throw new IllegalArgumentException(NOT_ENOUGH_BANK_BUD);
        }

        for (Budget b : budgets) {
            if (b.getName().equals(newBudgetName) && !b.getName().equals(budget.getName())) {
                throw new IllegalArgumentException(NAME_USED);
            }
        }

        // update the budget
        budget.setRemaining(newAllocated - (budget.getAllocated() - budget.getRemaining()));
        budget.setAllocated(newAllocated);
        budget.setName(newBudgetName);
        projectedSpend = projectedSpend + allocatedDifference;
    }


    /**
     * Remove a budget from user budgets
     * @param budget budget to remove
     */
    public void removeBudget(Budget budget){
        budgets.remove(budget);
        projectedSpend = projectedSpend - budget.getRemaining();
    }

    // getters and setters

    public double getTargetSaving() {
        return targetSaving;
    }

    public void setTargetSaving(double targetSaving) {
        this.targetSaving = targetSaving;
    }

    public double getLowBankWarning() {
        return lowBankWarning;
    }

    public void setLowBankWarning(double lowBugdetWarning) {
        this.lowBankWarning = lowBugdetWarning;
    }

    public double getBankAmount() {
        return bankAmount;
    }

    public void setBankAmount(double bankAmount) {
        this.bankAmount = bankAmount;
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    public double getProjectedSpend() {
        return projectedSpend;
    }

    public void setProjectedSpend(double projectedSpend) {
        this.projectedSpend = projectedSpend;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
