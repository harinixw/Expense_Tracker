const mongoose = require("mongoose");

const recurringExpenseSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true
  },

  merchant: String,
  amount: Number,
  category: String,
  currency: { type: String, default: "INR" },

  frequency: {
    type: String,
    enum: ["daily", "weekly", "monthly"],
    default: "monthly"
  },

  startDate: {
    type: Date,
    required: true
  },

  nextDueDate: {
    type: Date,
    required: true
  },

  isActive: {
    type: Boolean,
    default: true
  },

  paymentMethod: {
    type: String,
    default: "unknown"
  },

  createdAt: {
    type: Date,
    default: Date.now
  }
});

module.exports = mongoose.model("RecurringExpense", recurringExpenseSchema);