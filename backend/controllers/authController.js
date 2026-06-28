  const User = require("../models/User");         //imports
  const bcrypt = require("bcryptjs");
  const jwt = require("jsonwebtoken");

  //REGISTER
  exports.register = async (req, res) => {        //creating a function called register and making it available outside this file
    try {
      const { username, password } = req.body;

      const existing = await User.findOne({ username });
      if (existing) return res.status(400).json({ msg: "User exists" });

      const hashed = await bcrypt.hash(password, 10);

      const user = new User({ username, password: hashed });
      await user.save();

      res.json({ msg: "User registered" });
    } catch (err) {
      res.status(500).json(err);
    }
  };

  // LOGIN
  exports.login = async (req, res) => {
    try {
      const { username, password } = req.body;

      const user = await User.findOne({ username });
      if (!user) return res.status(400).json({ msg: "Invalid credentials" });

      const isMatch = await bcrypt.compare(password, user.password);
      if (!isMatch) return res.status(400).json({ msg: "Invalid credentials" });

      const token = jwt.sign(
        { id: user._id },
        process.env.JWT_SECRET,
        { expiresIn: "1d" }
      );

      res.json({ token });
    } catch (err) {
      res.status(500).json(err);
    }
  };

  exports.loginWithToken = async (req, res) => {

  try {

    const user = await User.findById(req.user.id).select("-password");

    if (!user) {
      return res.status(404).json({
        msg: "User not found"
      });
    }

    res.json({
      user
    });

  } catch (err) {
    console.log(err)
    res.status(500).json({
      msg: "Server error"
    });

  }
};