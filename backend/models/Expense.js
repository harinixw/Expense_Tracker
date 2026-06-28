const mongoose = require("mongoose");

const expenseSchema = new mongoose.Schema({

  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true
  },

  amount: {
    type: Number,
    required: true
  },

  category: {
    type: String,
    required: true
  },

  merchant: {
    type: String,
    default: ""
  },

  currency: { 
    type: String, 
    default: "INR" 
  },
  
  // Description (manual or from receipt)
  note: {
    type: String,
    default: ""
  },

  date: {
    type: Date,
    required: true
  },

  // Parsed data from CV
  receiptData: {
    type: Object   // { amount, merchant, date }
  },

  // Recurring flags
  isRecurring: {
    type: Boolean,
    default: false
  },

  recurringId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "RecurringExpense"
  },

  paymentMethod: {
    type: String, // cash, card, upi
    default: "unknown"
  },

  createdAt: {
    type: Date,
    default: Date.now
  }

});

module.exports = mongoose.model("Expense", expenseSchema);