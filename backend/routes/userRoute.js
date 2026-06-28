const express = require("express");
const router = express.Router();

const { updateUser, addCategory, deleteCategory, getCategories } = require("../controllers/userController");
const authMiddleware = require("../middleware/authMiddleware");
const {getAnalytics} = require("../controllers/analyticsController");


router.put("/", authMiddleware, updateUser);
router.post("/category", authMiddleware, addCategory);
router.delete("/category", authMiddleware, deleteCategory);
router.get("/category", authMiddleware, getCategories);
router.get("/analytics", authMiddleware, getAnalytics);

module.exports = router;