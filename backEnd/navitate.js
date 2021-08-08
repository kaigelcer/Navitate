const express = require("express");
const { read } = require("fs");
const app = express();
const mongoClient = require("mongodb").MongoClient;
const url = "mongodb://localhost:27017";

app.use(express.json()); //enable json parsing

const server = app.listen(3000);
console.log("listening on port 3000");

module.exports = server;

var myDb;


mongoClient.connect(url, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
}, (err, db) => {

    myDb = db.db("myDb");
    users = myDb.collection("users");

    app.get('/', function (req, res) {
        res.send("listening");
    });


    app.post("/user", async (req, res) => {

        if (req.body.type == "login"){
            users.find().forEach(function (myDoc) {
                if (myDoc.username == req.body.username) {
                    if (myDoc.password == req.body.password) {
                        res.status(200).send();
                        return;
                    } else {
                        res.status(201).send();
                        return;
                    }
                }
            });
            res.status(202).send();
        } else if (req.body.type == "newUser") {
            users.find().forEach(function (myDoc) {
                if (myDoc.username == req.body.username) {
                    res.status(201).send();
                    return;
                }
            });
            const newUser = {
                username: req.body.username,
                password: req.body.password,
            }
            users.insertOne(newUser);
            res.status(200).send();
        }

    });

    //handles post requests
    app.post("/navitation", async (req, res) => {

        collection = myDb.collection(req.body.title + "_by_" + req.body.username);
        collection.deleteMany({});
        
        if (req.body.type == "metadata"){
            const userAndTitle = {
                username: req.body.username,
                password: req.body.password,
                title: req.body.title,
            }
            collection.insertOne(userAndTitle);
        } else if (req.body.type == "circle") {
            const newCircle = {
                type: req.body.type,
                latitude: req.body.latitude,
                longitude: req.body.longitude,
                strokeColour: req.body.strokeColour,
                strokeWidth: req.body.strokeWidth,
                strokePattern: req.body.strokePattern,
                fillColour: req.body.fillColour,
                radius: req.body.radius
            };
            collection.insertOne(newCircle);
            console.log(newCircle);
        } else if (req.body.type == "line" || req.body.type == "drawing") {
            const newLine = {
                type: req.body.type,
                points: req.body.points,
                strokeColour: req.body.strokeColour,
                strokeWidth: req.body.strokeWidth,
                strokePattern: req.body.strokePattern,
            };
            collection.insertOne(newLine);
            console.log(newLine);
        } else if (req.body.type = "marker") {
            const newMarker = {
                type: req.body.type,
                latitude: req.body.latitude,
                longitude: req.body.longitude,
                colour: req.body.colour
            };
            console.log(newMarker);
            collection.insertOne(newMarker);
        }
        collection.find().toArray((err, result) => {
            console.log(result);
            res.send(result);
            });
        res.status(200).send();
        

    });

    app.get("/navitations", async (req, res) => {
        
        myDb.listCollections().toArray(function(err, collInfos) {
            var collectionNames = [];
            console.log(collInfos);
            collInfos.forEach(element => {
                if (element.name != "users"){
                    collectionNames.push(element.name);
                };
            });
            res.send(collectionNames);
        });
        
        
    });

    app.get("/navitation/:title", async (req, res) => {
        collection = myDb.collection(req.params.title);
        collection.find().toArray((err, result) => {
            res.send(result);
            });
    });



});

