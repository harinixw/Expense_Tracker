// routes/testRoutes.js
const express = require("express");
const router = express.Router();

const { runRecurring } = require("../utils/recurringJob");

router.get("/run-recurring", async (req, res) => {
  await runRecurring();
  res.send("Recurring job executed");
});

module.exports = router;