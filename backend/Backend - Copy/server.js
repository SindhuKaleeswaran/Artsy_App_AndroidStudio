require('dotenv').config();

const express = require("express");
const axios = require("axios");
const mongoose = require("mongoose");
const path = require("path");
const user = require("./config");
const bodyParser = require("body-parser");
const cors = require('cors');
const bcrypt = require('bcrypt');
const { error } = require("console");
const jwt = require('jsonwebtoken');
const cookieParser = require("cookie-parser");
const gravatar = require('gravatar');

const app = express();

app.use(express.json());
app.use(cookieParser());
app.use(cors({
  origin: 'http://localhost:4200/',
  credentials: true,
}));

mongoose.connect(process.env.URI)
.then(() =>{
    //console.log("Connected to MongoDB");
}).catch((err) => console.error(err));

app.post('/register', async(req, res) => {
    try {
        const {fullName, emailID, password} = req.body;
        const existingUser = await user.findOne({Username: emailID});

        if(existingUser){
            return res.status(400).json({ message: "User already exists" });
        }else{
            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(password, salt);

            const userProfile = gravatar.url(emailID, { s: '200', r: 'pg', d: 'identicon' });

            const newUser = new user({
                Fullname: fullName,
                Username: emailID,
                Password: hashedPassword,
                favourites: [],
                userProfile: userProfile
            });

            await newUser.save();

            res.status(201).json({ message: "User registered successfully", user: newUser });
        }
    } catch (error) {
        console.error("Error during registration:", error);
        res.status(500).json({ message: "Server error", error: error.message });
    }
});


app.post('/login', async(req,res) => {
    try {
        const Username = req.body.emailID;
        let Password = req.body.password;

        //console.log('Received data:', {Username, Password });

        const isCheck = await user.findOne({Username: req.body.emailID});
        if(!isCheck){
            return res.status(400).json({ message: "Invalid username or password" });
        }
        const isPassword = await bcrypt.compare(req.body.password, isCheck.Password);
        if (!isPassword) {
            return res.status(400).json({ message: "Invalid username or password" });
        }
        
        const jwtoken = jwt.sign({
            user: {
                _id: isCheck._id
            }
        },process.env.JWT_SECRET, {expiresIn: '1h'}
        );

        res.cookie('jwtoken', jwtoken, { 
            maxAge: 3600000,
        });

        res.cookie('fullname', isCheck.Fullname, { 
            maxAge: 3600000,
        });

        res.cookie('username', isCheck.Username, { 
            maxAge: 3600000,
        });

        res.cookie('favourites', JSON.stringify(isCheck.favourites), { 
            maxAge: 3600000,
        });

        res.cookie('fav', JSON.stringify(isCheck.favourites), { 
            maxAge: 3600000,
        });

        //console.log("Fullname is: ",isCheck.Fullname);
        return res.status(200).cookie('favouriteee', JSON.stringify(isCheck.favourites), { 
            maxAge: 3600000,
        }).json({ jwtoken,  message: "Login successful" , 'fullname' : isCheck.Fullname, 
            'username' : isCheck.Username, 'favv': isCheck.favourites, 'prfpic': isCheck.userProfile});
    } catch (error) {
        console.error("Login Error:", error);
        res.status(500).json({ message: "Server error", error: error.message });
    }
})

const authenticateToken = (req, res, next) => {
    let token;
    let authHeader = req.headers.authorization || req.headers.Authorization;

    if (authHeader && authHeader.startsWith("Bearer")) {
        token = authHeader.split(" ")[1];
        
        // Verify the token's validity and expiration
        jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
            if (err) {
                console.error('Token expired or invalid:', err);
                return res.status(403).json({ message: 'Token is not valid' });
            }

            req.user = decoded;
            //console.log("Decoded token:", decoded);
            next();
        });
    } else {
        return res.status(401).json({ message: 'Authorization token missing' });
    }


  };
  
  // Example protected route
app.get('/protected', authenticateToken, (req, res) => {
res.json({ message: 'This is a protected route', user: req.user });
});

app.get('/me', authenticateToken, (req, res) => {
    res.json({ 
      message: 'User profile data',
      user: req.user
    });
  });


//To test the above function, go to Postman, typr out the fields in raw json format and send them. This should be reflected in MongoDB
app.post('/fav', authenticateToken, async (req, res) => {
    try {
        const { emailID, artistID, flag } = req.body;

        if (!emailID || !artistID || typeof flag !== 'number') {
            return res.status(400).json({ message: "Missing or invalid parameters" });
        }

        const existingUser = await user.findOne({ Username: emailID });

        if (!existingUser) {
            return res.status(404).json({ message: "User not found" });
        }

        if (flag === 1) {
            const alreadyExists = existingUser.favourites.some(fav =>
                fav.artistId.toString() === artistID
            );

            if (alreadyExists) {
                return res.status(200).json({ message: "Artist already in favourites" });
            }

            existingUser.favourites.push({
                artistId: artistID,
                addedAt: new Date()
            });

            await existingUser.save();
            return res.status(200).json({ message: "Artist added to favourites" });

        } else if (flag === 0) {
            const originalLength = existingUser.favourites.length;

            existingUser.favourites = existingUser.favourites.filter(fav =>
                fav.artistId.toString() !== artistID
            );

            if (existingUser.favourites.length === originalLength) {
                return res.status(404).json({ message: "Artist not found in favourites" });
            }

            await existingUser.save();
            return res.status(200).json({ message: "Artist removed from favourites" });

        } else {
            return res.status(400).json({ message: "Invalid flag value! Must be 0 or 1." });
        }

    } catch (error) {
        console.error("Error handling favourites:", error);
        return res.status(500).json({ message: "Server error", error: error.message });
    }
});



app.delete('/user', authenticateToken, async (req,res) => {
    try{
        const { emailID } = req.body;
        const existingUser = await user.findOne({ Username: emailID });
        if (!existingUser) {
            return res.status(404).json({ message: 'User not found' });
        }
        await user.deleteOne({ Username: emailID });

        return res.status(200).json({ message: 'User successfully deleted' });

    } catch(error){
        console.error("Error deleting user:", error);
        res.status(500).json({ message: "Server error", error: error.message });
    }
});


const Authenticate = async() =>{
    const url = 'https://api.artsy.net/api/tokens/xapp_token'
    const data = {
        'client_id' : '59f1153a46d953b45ba2' ,
        'client_secret' : 'e8fe0702caf7ceadc7c4ebc24da08b1a'
    }
    const headers = {'Content-Type': 'application/json'}

    const response = await axios.post(url, data, { headers });
    if (response.status === 201) {
        //console.log(response.data.token);
        return response.data.token;
      } else {
        throw new Error("Cannot fetch token!");
      }
    
}

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

    const artistDetail = {
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

        categoriesData.push({
            categoryName : name,
            categoryThumbnail : thumbnail
        })
    })

    //console.log(response.status);

    if(response.status === 200){
        return res.json(categoriesData);   
    }else{
        throw new Error("Cannot fetch artwork");
    }
})

app.use(express.static(path.join(__dirname, 'public')));

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, './public', 'index.html'));
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});