const User = require("../models/User"); 
const Expense = require("../models/Expense");

exports.createGoal = async (req, res) => {
  try {
    const { type, title, targetAmount, category, endDate } = req.body;

    if (!type || !title || !targetAmount) {
      return res.status(400).json({ msg: "Missing required fields" });
    }
     edate = endDate;
    if (!edate){
        const now = new Date();
        if(now.getMonth()==12){
            edate = new Date(now.getFullYear()+1, 1, now.getDate());
        } else {
            edate = new Date(now.getFullYear(), now.getMonth()+1, now.getDate());
        }       
    }

    const newGoal = {
      type,
      title,
      targetAmount,
      category: category || null,
      endDate
    };

    const user = await User.findById(req.user.id);
    user.goals.push(newGoal);

    await user.save();

    res.status(201).json({ msg: "Goal created", goal: newGoal });

  } catch (err) {
    console.log(err);
    res.status(500).json({ msg: "Server error " });
  }
};

exports.getGoals = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);

    res.json(user.goals);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.updateGoal = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);

    const goal = user.goals.id(req.params.id);

    if (!goal) {
      return res.status(404).json({ msg: "Goal not found" });
    }

    Object.assign(goal, req.body);

    await user.save();

    res.json({ msg: "Goal updated", goal });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.deleteGoal = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);

    const goal = user.goals.id(req.params.id);

    if (!goal) {
      return res.status(404).json({ msg: "Goal not found" });
    }

    goal.remove();

    await user.save();

    res.json({ msg: "Goal deleted" });

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};

exports.getGoalProgress = async (req, res) => {
  try {
    const userId = req.user.id;

    const user = await User.findById(userId);
    const goals = user.goals;

    const expenses = await Expense.find({ userId });

    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    let results = [];

    for (let goal of goals) {

      let progress = 0;

      if (goal.type === "category_limit") {
        const total = expenses
          .filter(e =>
            e.category === goal.category &&
            e.date >= startOfMonth
          )
          .reduce((sum, e) => sum + e.amount, 0);

        progress = (total / goal.targetAmount) * 100;
      }

      if (goal.type === "budget") {
        const total = expenses
          .filter(e => e.date >= startOfMonth)
          .reduce((sum, e) => sum + e.amount, 0);

        progress = (total / goal.targetAmount) * 100;
      }

      if (goal.type === "savings") {
        const total = expenses
          .filter(e => e.date >= startOfMonth)
          .reduce((sum, e) => sum + e.amount, 0);

        const saved = user.budget - total;
        progress = (saved / goal.targetAmount) * 100;
      }

      if(progress>=100){
        goal.isCompleted = true;
      }

      if (goal.endDate && goal.endDate < now){
        console.log(goal.endDate)
        progress = 100;
      }

      results.push({
        ...goal._doc,
        progress: Math.min(progress, 100)
      });
    }

    res.json(results);

  } catch (err) {
    res.status(500).json({ msg: "Server error" });
  }
};