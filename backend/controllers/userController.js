const User = require("../models/User");

exports.updateUser = async (req, res) => {
  try {
    const { currency, budget, notifsEnabled } = req.body;

    const user = await User.findById(req.user.id);

    if (!user) {
      return res.status(404).json({ msg: "User not found" });
    }

    // update only provided fields
    if (currency !== undefined) user.currency = currency;
    if (budget !== undefined) user.budget = budget;
    if (notifsEnabled !== undefined) user.notifsEnabled = notifsEnabled;

    await user.save();

    res.json({ msg: "User updated", user });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.addCategory = async (req, res) => {
  try {
    const { category } = req.body;

    if (!category) {
      return res.status(400).json({ msg: "Category required" });
    }

    const user = await User.findById(req.user.id);

    // avoid duplicates
    if (user.category.includes(category)) {
      return res.status(400).json({ msg: "Category already exists" });
    }

    user.category.push(category);

    await user.save();

    res.json({ msg: "Category added", categories: user.category });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.deleteCategory = async (req, res) => {
  try {
    const { category } = req.body;

    const user = await User.findById(req.user.id);

    user.category = user.category.filter(c => c !== category);

    await user.save();

    res.json({ msg: "Category removed", categories: user.category });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.getCategories = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);

    res.json(user.category);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};