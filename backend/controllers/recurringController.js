// controllers/recurringController.js
const RecurringExpense = require("../models/RecurringExpense");

exports.createRecurring = async (req, res) => {
  try {
    const {
      merchant,
      amount,
      category,
      frequency,
      startDate,
      paymentMethod
    } = req.body;

    const recurring = new RecurringExpense({
      userId: req.user.id,
      merchant,
      amount,
      category,
      frequency,
      startDate,
      nextDueDate: startDate,
      paymentMethod
    });

    await recurring.save();

    res.status(201).json({ msg: "Recurring expense created", recurring });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.updateRecurring = async (req, res) => {
  try {
    const updated = await RecurringExpense.findOneAndUpdate(
      { _id: req.params.id, userId: req.user.id },
      req.body,
      { new: true }
    );

    res.json(updated);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.deleteRecurring = async (req, res) => {
  try {
    await RecurringExpense.findOneAndUpdate(
      { _id: req.params.id, userId: req.user.id },
      { isActive: false }
    );

    res.json({ msg: "Recurring expense stopped" });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.getRecurring = async (req, res) => {
  try {
    const data = await RecurringExpense.find({
      userId: req.user.id,
      isActive: true
    });

    res.json(data);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};