const mongoose = require("mongoose");

const userSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  currency: { type: String, required: false, default: "INR" },
  budget: { type: Number, required: false, default: 10000 },
  goals: [
  {
    type: {
      type: String,
      enum: ["savings", "category_limit", "budget"],
      required: true
    },

    title: {
      type: String,
      required: true
    },

    targetAmount: {
      type: Number,
      required: true
    },

    category: {
      type: String,
      default: null // only for category_limit goals
    },

    startDate: {
      type: Date,
      default: Date.now
    },

    endDate: {
      type: Date
    },

    isCompleted: {
      type: Boolean,
      default: false
    },

    createdAt: {
      type: Date,
      default: Date.now
    }
  }
  ],
  totalSpending: { type: Number, default: 0 },
  avgMonthlySpending: { type: Number, default: 0 },
  notifsEnabled: { type: Boolean, default: true },
  dateCreated: { type: Date, default: Date.now },
  category: [ { type: String } ]
});

module.exports = mongoose.model("User", userSchema);