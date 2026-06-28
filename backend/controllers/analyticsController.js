const Expense = require("../models/Expense");
const User = require("../models/User");

exports.getAnalytics = async (req, res) => {
  try {
    const userId = req.user.id;

    const user = await User.findById(userId);
    const expenses = await Expense.find({ userId });

    if (expenses.length === 0) {
      return res.json({
        totalSpending: 0,
        avgMonthlySpending: 0,
        insights: []
      });
    }

    const totalSpending = expenses.reduce((sum, e) => sum + e.amount, 0);

    let monthlyMap = {};
    let categoryMap = {};

    expenses.forEach(e => {
      const date = new Date(e.date);
      const monthKey = `${date.getFullYear()}-${date.getMonth()}`;

      // monthly
      if (!monthlyMap[monthKey]) monthlyMap[monthKey] = 0;
      monthlyMap[monthKey] += e.amount;

      // category
      if (!categoryMap[e.category]) categoryMap[e.category] = 0;
      categoryMap[e.category] += e.amount;
    });

    // AVG MONTHLY
    const months = Object.keys(monthlyMap).length;
    const avgMonthlySpending = totalSpending / months;

    // INSIGHTS LOGIC
    let insights = [];

    // 1. Budget check
    if (totalSpending > user.budget) {
      insights.push("You have exceeded your budget");
    } else if (totalSpending > 0.8 * user.budget) {
      insights.push("You are close to your budget limit");
    }

    const nowdate = new Date();
    const expectedSpending = (30 / (nowdate.getDate()))*monthlyMap[`${nowdate.getFullYear()}-${nowdate.getMonth()}`];
    insights.push(`Your expected spendings for this month are: ${expectedSpending}`);
    // 2. Top category
    const topCategory = Object.keys(categoryMap).reduce((a, b) =>
      categoryMap[a] > categoryMap[b] ? a : b
    );

    insights.push(`Highest spending is on ${topCategory}`);

    // 3. High spending alert
    if (avgMonthlySpending > user.budget) {
      insights.push("Your average spending is higher than your budget");
    }

    // 4. Suggest savings
    const potentialSavings = totalSpending - user.budget;
    if (potentialSavings > 0) {
      insights.push(`You could save ₹${potentialSavings} by sticking to your budget`);
    }

    res.json({
      totalSpending,
      avgMonthlySpending: Math.round(avgMonthlySpending),
      categoryBreakdown: categoryMap,
      insights
    });

  } catch (err) {
    console.log(err);
    res.status(500).json({ msg: "Server error" });
  }
};