require('dotenv').config();

const express = require("express");
const axios = require("axios");
const mongoose = require("mongoose");
const path = require("path");
const user = require("./config");
const bodyParser = require("body-parser");
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cookieParser = require("cookie-parser");
const gravatar = require('gravatar');
const app = express();

app.use(express.json());
app.use(cookieParser());
app.use(cors({
  origin: 'http://localhost:4200',
  credentials: true,
}));

mongoose.connect(process.env.URI)
.then(() => console.log("Connected to MongoDB"))
.catch((err) => console.error(err));

const Authenticate = async () => {
  const url = 'https://api.artsy.net/api/tokens/xapp_token';
  const data = {
        'client_id' : '59f1153a46d953b45ba2' ,
        'client_secret' : 'e8fe0702caf7ceadc7c4ebc24da08b1a'
    }
  const headers = { 'Content-Type': 'application/json' };
  const response = await axios.post(url, data, { headers });
  if (response.status === 201) return response.data.token;
  throw new Error("Cannot fetch token!");
};

const authenticateToken = (req, res, next) => {
  let token = req.cookies?.jwtoken || (req.headers.authorization?.startsWith("Bearer") ? req.headers.authorization.split(" ")[1] : null);
  if (!token) return res.status(401).json({ message: 'Authorization token missing' });
  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) return res.status(403).json({ message: 'Token is not valid' });
    req.user = decoded.user;
    next();
  });
};

app.post('/register', async (req, res) => {
    try {
      const { fullName, emailID, password } = req.body;
  
      const existingUser = await user.findOne({ Username: emailID });
      if (existingUser) {
        return res.status(400).json({ message: "User already exists" });
      }
  
      const hashedPassword = await bcrypt.hash(password, 10);
      const userProfile = gravatar.url(emailID, { s: '200', r: 'pg', d: 'identicon' });
  
      const newUser = new user({
        Fullname: fullName,
        Username: emailID,
        Password: hashedPassword,
        favourites: [],
        userProfile
      });
  
      await newUser.save();
  
      const jwtoken = jwt.sign(
        { user: { _id: newUser._id, email: newUser.Username } },
        process.env.JWT_SECRET,
        { expiresIn: '1h' }
      );
  
      res.cookie('jwtoken', jwtoken, {
        maxAge: 3600000,
        httpOnly: true,
        sameSite: 'Lax',
      });
  
      res.status(200).json({
        jwtoken,
        message: "User registered successfully",
        fullname: newUser.Fullname,
        username: newUser.Username,
        favv: newUser.favourites,
        prfpic: newUser.userProfile
      });
  
    } catch (error) {
      res.status(500).json({ message: "Server error", error: error.message });
    }
  });
  
app.post('/login', async (req, res) => {
  try {
    const { emailID, password } = req.body;
    const isCheck = await user.findOne({ Username: emailID });
    if (!isCheck || !(await bcrypt.compare(password, isCheck.Password))) {
      return res.status(400).json({ message: "Invalid username or password" });
    }

    const jwtoken = jwt.sign({ user: { _id: isCheck._id, email: isCheck.Username } }, process.env.JWT_SECRET, { expiresIn: '1h' });
    res.cookie('jwtoken', jwtoken, { maxAge: 3600000 });

    res.status(200).json({
      jwtoken,
      message: "Login successful",
      fullname: isCheck.Fullname,
      username: isCheck.Username,
      favv: isCheck.favourites,
      prfpic: isCheck.userProfile
    });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
});

app.get('/fav', authenticateToken, async (req, res) => {
  try {
    const emailID = req.user.email;
    const existingUser = await user.findOne({ Username: emailID });
    if (!existingUser) return res.status(404).json({ message: 'User not found' });

    const token = await Authenticate();
    const enriched = await Promise.all(existingUser.favourites.map(async fav => {
      try {
        const artistRes = await axios.get(`https://api.artsy.net/api/artists/${fav.artistId}`, {
          headers: { 'X-API-Key': token }
        });
        const artist = artistRes.data;
        return {
          artistId: fav.artistId,
          addedAt: fav.addedAt,
          name: artist.name || null,
          nationality: artist.nationality || null,
          birthday: artist.birthday || null
        };
      } catch {
        return { artistId: fav.artistId, addedAt: fav.addedAt };
      }
    }));

    res.status(200).json({ favorites: enriched });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
});

app.post('/fav', authenticateToken, async (req, res) => {
    console.log(" /fav called with", req.body, "user:", req.user?.email);

    try {
      const emailID = req.user.email;
      const { artistID, flag } = req.body;
  
      const existingUser = await user.findOne({ Username: emailID });
      if (!existingUser) return res.status(404).json({ message: "User not found" });
      console.log("/fav hit", { emailID, artistID, flag });
  
      if (flag === 1) {
        const alreadyExists = existingUser.favourites.some(
          fav => fav.artistId.toString() === artistID
        );
        if (!alreadyExists) {
          existingUser.favourites.push({ artistId: artistID, addedAt: new Date() });
          await existingUser.save();
        }
        return res.status(200).json({ message: "Artist added to favourites" });
      } else if (flag === 0) {
        const originalLength = existingUser.favourites.length;
        existingUser.favourites = existingUser.favourites.filter(
          fav => fav.artistId.toString() !== artistID
        );
        if (existingUser.favourites.length < originalLength) {
          await existingUser.save();
        }
        return res.status(200).json({ message: "Artist removed from favourites" });
      } else {
        return res.status(400).json({ message: "Invalid flag value! Must be 0 or 1." });
      }
    } catch (error) {
      console.error("Error in /fav:", error);
      res.status(500).json({ message: "Server error", error: error.message });
    }
  });
  

app.delete('/user', authenticateToken, async (req, res) => {
  try {
    const { emailID } = req.body;
    const existingUser = await user.findOne({ Username: emailID });
    if (!existingUser) return res.status(404).json({ message: 'User not found' });
    await user.deleteOne({ Username: emailID });
    res.status(200).json({ message: 'User successfully deleted' });
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
});



app.get('/search', async(req,res) =>{
    const token = await Authenticate();
    const query = req.query.artistName;  
    const url = 'https://api.artsy.net/api/search'
    const params = {
        'q' : query,
        'size' : 10,
        'type' : 'artist'
    }
    const headers = {
        'X-API-Key' : token,
        'Content-Type': 'application/json'
    }

    

    const response = await axios.get(url ,{ params, headers });

    const artistSearch = response.data;
    //console.log(response.status);
    //console.log(response.data);

    let artistMapping = [];

    artistSearch._embedded.results.forEach((artist) => {
        let artistId = artist._links.self.href.split("/").pop();
        let artistName = artist.title;
        let artistImage = artist._links.thumbnail.href;
    
        artistMapping.push({
            name: artistName,
            id: artistId,
            image: artistImage,
            isFavourite: false
        });
    });
    
    //let artist_id = artist_mapping.map(artist => artist.id);

    if(response.status === 200){
        return res.json(artistMapping);
    }else{
        throw new Error("Cannot fetch artists");
    }
})

app.get('/artist_id', async(req,res) => {
    const token = await Authenticate();
    const artist_id = req.query.artistID;
    const url = `https://api.artsy.net/api/artists/${artist_id}`;
    const headers = {
        'X-API-Key' : token,
        'Content-Type': 'application/json'
    }

    const response = await axios.get(url, {headers});

    const artistArticle = response.data;
    //console.log(token);

    const artistDetail = {
        id: artist_id,
        artistName : artistArticle.name,
        birthday  : artistArticle.birthday,
        deathday : artistArticle.deathday,
        nationality: artistArticle.nationality,
        biography : artistArticle.biography,
        thumbnail : artistArticle._links.thumbnail ? artistArticle._links.thumbnail.href : null,
        similarArtists : []
    }

    if(artistArticle._links.similar_artists && artistArticle._links.similar_artists.href){
        const similarArtistURL = artistArticle._links.similar_artists.href;
        const similarResponse = await axios.get(similarArtistURL, {headers});

        artistDetail.similarArtists = similarResponse.data._embedded.artists.map(artist => ({
            id: artist.id,
            name: artist.name,
            thumbnail: artist._links.thumbnail ? artist._links.thumbnail.href : null
        }));
    }

    //console.log(response.status);
    //console.log(response.data);
    if(response.status === 200){
        return res.json(artistDetail);
    }else{
        throw new Error("Cannot fetch artist detail");
    }
})

app.get('/artworks', async(req,res) => {
    const token = await Authenticate();
    let query = req.query.artist_id;
    const url = 'https://api.artsy.net/api/artworks';
    const params = {
        'artist_id' : query,
        'size' : 10
    }
    const headers = {
        'X-API-Key' : token,
        'Content-Type': 'application/json'
    }

    const response = await axios.get(url ,{ params, headers });

    const artworks = response.data;
    let artworkDetail = []

    artworks._embedded.artworks.forEach((artistID) =>{
        let id = artistID.id;
        let title = artistID.title;
        let date = artistID.date;
        let image = artistID._links.thumbnail.href;

        artworkDetail.push({
            artworkID : id,
            artworkTitle : title,
            artworkDate: date,
            artworkImage : image
        })
    })

    //console.log(response.status);

    if(response.status === 200){
        return res.json(artworkDetail);   
    }else{
        throw new Error("Cannot fetch artwork");
    }
})

app.get('/categories', async(req,res) =>{
    const token = await Authenticate();
    let query = req.query.artwork_id;
    const url = 'https://api.artsy.net/api/genes';
    const params = {
        'artwork_id' : query,
        'size' : 10
    }
    const headers = {
        'X-API-Key' : token,
        'Content-Type': 'application/json'
    }

    const response = await axios.get(url ,{ params, headers })

    const categories = response.data;
    let categoriesData = []

    categories._embedded.genes.forEach((artistCategory) => {
        let name = artistCategory.name;
        let thumbnail = artistCategory._links.thumbnail.href;
        let description = artistCategory.description || "No description available.";

        categoriesData.push({
            categoryName : name,
            categoryThumbnail : thumbnail,
            categoryDescription: description
        })
    })
    
    //console.log(response.status);

    if(response.status === 200){
        return res.json(categoriesData);   
    }else{
        throw new Error("Cannot fetch artwork");
    }
})

const os = require('os');

function getLocalIP() {
    const networkInterfaces = os.networkInterfaces();
    for (let interfaceName in networkInterfaces) {
        for (let interfaceInfo of networkInterfaces[interfaceName]) {
            // Skip over interfaces that are not IPv4 addresses or are internal (loopback)
            if (interfaceInfo.family === 'IPv4' && !interfaceInfo.internal) {
                return interfaceInfo.address;
            }
        }
    }
    return null; // If no local IP is found
}

console.log('Local IP Address:', getLocalIP());
app.listen(3000, () => {
    console.log('Success');
  });

