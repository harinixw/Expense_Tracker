// utils/recurringJob.js
const Expense = require("../models/Expense");
const RecurringExpense = require("../models/RecurringExpense");

const runRecurring = async () => {
  const today = new Date();

  const dueExpenses = await RecurringExpense.find({
    isActive: true,
    nextDueDate: { $lte: today }
  });

  for (let item of dueExpenses) {

    await Expense.create({
      userId: item.userId,
      amount: item.amount,
      category: item.category,
      merchant: item.merchant,
      date: item.nextDueDate,
      isRecurring: true,
      recurringId: item._id
    });

    // update next date
    let next = new Date(item.nextDueDate);

    if (item.frequency === "monthly") next.setMonth(next.getMonth() + 1);
    if (item.frequency === "weekly") next.setDate(next.getDate() + 7);
    if (item.frequency === "daily") next.setDate(next.getDate() + 1);

    item.nextDueDate = next;
    await item.save();
  }

  console.log("Recurring job ran");
};

module.exports = { runRecurring };