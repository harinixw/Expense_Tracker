const express = require("express");
const router = express.Router();

const {
  createRecurring,
  updateRecurring,
  deleteRecurring,
  getRecurring
} = require("../controllers/recurringController");

const authMiddleware = require("../middleware/authMiddleware");

router.post("/", authMiddleware, createRecurring);
router.get("/", authMiddleware, getRecurring);
router.put("/:id", authMiddleware, updateRecurring);
router.delete("/:id", authMiddleware, deleteRecurring);

module.exports = router;