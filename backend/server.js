const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const authRoutes = require("./routes/authRoutes");
const expenseRoute = require("./routes/expenseRoute")
const recurringRoute = require("./routes/recurringRoute")
const goalRoutes = require("./routes/goalRoute");
const userRoute = require("./routes/userRoute");

const testRoute = require("./routes/testRoute");


const app = express();

app.use(cors());
app.use(express.json());

app.use("/api/auth", authRoutes);
app.use("/api/expense", expenseRoute );
app.use("/api/recurring", recurringRoute);
app.use("/api/goals", goalRoutes);
app.use("/api/user", userRoute);

app.use("/api/test", testRoute);


mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log("DB connected"))
  .catch(err => console.log(err));

app.listen(5000, () => console.log("Server running"));

