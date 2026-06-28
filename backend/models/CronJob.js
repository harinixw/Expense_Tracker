const Expense = require("../models/Expense");
const RecurringExpense = require("../models/RecurringExpense");

const runRecurring = async () => {
  const today = new Date();

  const dueExpenses = await RecurringExpense.find({
    isActive: true,
    nextDueDate: { $lte: today }
  });

  for (let item of dueExpenses) {

    // create actual expense
    await Expense.create({
      userId: item.userId,
      amount: item.amount,
      category: item.category,
      merchant: item.merchant,
      date: item.nextDueDate,
      isRecurring: true,
      recurringId: item._id
    });

    // update next due date
    let next = new Date(item.nextDueDate);

    if (item.frequency === "monthly") next.setMonth(next.getMonth() + 1);
    if (item.frequency === "weekly") next.setDate(next.getDate() + 7);
    if (item.frequency === "daily") next.setDate(next.getDate() + 1);

    item.nextDueDate = next;
    await item.save();
  }
};

const cron = require("node-cron");
const { runRecurring } = require("./utils/recurringJob");

// runs every day at midnight
cron.schedule("0 0 * * *", runRecurring);