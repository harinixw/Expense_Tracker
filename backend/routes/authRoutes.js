const express = require("express");
const router = express.Router();
const { register, login, loginWithToken } = require("../controllers/authController");       //importing the functions you wrote earlier
const authMiddleware = require("../middleware/authMiddleware");

router.post("/register", register);
router.post("/login", login);
router.get("/logwtok", authMiddleware, loginWithToken);

module.exports = router;