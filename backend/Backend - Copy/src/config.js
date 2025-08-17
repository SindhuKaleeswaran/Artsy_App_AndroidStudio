const mongoose = require("mongoose");

const userSchema = new mongoose.Schema({
  Fullname: { type: String, required: true },
  Username: { type: String, required: true, unique: true },
  Password: { type: String, required: true },
  favourites: [{
    artistId: { type: mongoose.Schema.Types.ObjectId, ref: 'Artist', required: true },
    addedAt: { type: Date, default: Date.now }
  }],
  userProfile: { type: String, default: '' }
});

module.exports = mongoose.model("User", userSchema, "Users");
