// controllers/expenseController.js
const Expense = require("../models/Expense");

exports.createExpense = async (req, res) => {
  try {
    const {
      amount,
      category,
      merchant,
      currency,
      note,
      date,
      receiptData,
      isRecurring,
      recurringId,
      paymentMethod
    } = req.body;

    if (!amount || !category || !date) {
      return res.status(400).json({
        msg: "Amount, category and date are required"
      });
    }

    const expense = new Expense({
      userId: req.user.id,   // from JWT middleware

      amount,
      category,
      merchant: merchant || "",
      currency: currency || "INR",
      note: note || "",
      date,

      receiptData: receiptData || {},

      isRecurring: isRecurring || false,
      recurringId: recurringId || null,

      paymentMethod: paymentMethod || "unknown"
    });

    // Save to DB
    await expense.save();

    res.status(201).json({
      msg: "Expense added successfully",
      expense
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({
      msg: "Server error"
    });
  }
};

exports.getExpenses = async (req, res) => {
    try {
    const {
      category,
      startDate,
      endDate,
      paymentMethod,
      isRecurring,
      search,
      sortBy
    } = req.query;

    let filter = {
      userId: req.user.id
    };

    if (startDate && endDate) {
      filter.date = {
        $gte: new Date(startDate),
        $lte: new Date(endDate)
      };
    }

    if (category) {
      filter.category = category;
    }

    if (paymentMethod) {
      filter.paymentMethod = paymentMethod;
    }

    if (isRecurring !== undefined) {
      filter.isRecurring = isRecurring === "true";
    }

    if (search) {
      filter.$or = [
        { merchant: { $regex: search, $options: "i" } },
        { note: { $regex: search, $options: "i" } }
      ];
    }

    let sortOption = { date: -1 }; // default latest first

    if (sortBy === "amount_asc") sortOption = { amount: 1 };
    if (sortBy === "amount_desc") sortOption = { amount: -1 };
    if (sortBy === "oldest") sortOption = { date: 1 };

    const expenses = await Expense.find(filter).sort(sortOption);

    res.json(expenses);

  } catch (err) {
    console.error(err);
    res.status(500).json({ msg: "Server error" });
  }
};

exports.updateExpense = async (req, res) => {
  try {
    const updated = await Expense.findOneAndUpdate(
      { _id: req.params.id, userId: req.user.id },
      req.body,
      { new: true }
    );

    if (!updated) {
      return res.status(404).json({ msg: "Expense not found" });
    }

    res.json(updated);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.deleteExpense = async (req, res) => {
  try {
    const deleted = await Expense.findOneAndDelete({
      _id: req.params.id,
      userId: req.user.id
    });

    if (!deleted) {
      return res.status(404).json({ msg: "Expense not found" });
    }

    res.json({ msg: "Expense deleted" });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};