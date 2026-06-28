const express = require("express");
const router = express.Router();

const {
  createGoal,
  getGoals,
  updateGoal,
  deleteGoal,
  getGoalProgress
} = require("../controllers/goalController");

const authMiddleware = require("../middleware/authMiddleware");

router.post("/", authMiddleware, createGoal);
router.get("/", authMiddleware, getGoals);
router.put("/:id", authMiddleware, updateGoal);
router.delete("/:id", authMiddleware, deleteGoal);
router.get("/progress", authMiddleware, getGoalProgress);

module.exports = router;