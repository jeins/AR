'use strict';

let express = require('express'),
    fs = require('fs'),
    buffer = require('buffer'),
    multer = require('multer'),
    router = express.Router(),
    models = require('./models')
    ;

let imageApiPrefix = '/api/image/';
let imgPath = __dirname + '/../public/images/';

/**
 * get api version
 */
router.get('/api/version', (req, res)=>{
    res.json({version: '1.0.0'});
});

/**
 * get image from name and file extension
 * exp request: /api/image/41758/png
 */
router.get(imageApiPrefix + ':name/:extension', (req, res)=>{
    let imgExtension = req.params.extension;
    let imgName = req.params.name + '.' + imgExtension;

    fs.readFile(imgPath + imgName, (err, data)=>{
        if(err) throw err;

        res.writeHead(200, {'Content-Type': imgExtension});
        res.end(data, 'binary');
    })
});

/**
 * get image list by lat & lon
 *  exp requestBody: {"location":{"latitude": 52.456925, "longitude": 13.526658}}
 */
router.post(imageApiPrefix + 'list', (req, res)=>{
    let filter = {
        latitude: req.body.location.latitude,
        longitude: req.body.location.longitude
    };

    models.images
        .findOne({
            order: 'createdAt DESC',
            where: filter
        })
        .then((images)=>{
            res.json(images);
        })
});

/**
 * get data from nearest location
 * distance < 1 (mile = 1,6km)
 */
router.get(imageApiPrefix + 'nearest-from/:latitude/:longitude', (req, res)=>{
    let latitude = req.params.latitude,
        longitude = req.params.longitude;

    models.sequelize
        .query('SELECT *, ' +
            'SQRT( POW(69.1 * (latitude - '+ latitude +'), 2) + POW(69.1 * ('+ longitude +' - longitude) * ' +
            'COS(latitude / 57.3), 2)) AS distance ' +
            'FROM images HAVING distance < 1 ORDER BY distance')
        .spread((results, metadata) =>{
            res.json(results);
        });
});

/**
 * add image with image information
 * imgData must to base64 encrypted
 * imgData contains location & message
 * exp imgData: {"location":{"latitude": 52.456925, "longitude": 13.526658}, "message": "hello world"}
 */
router.post(imageApiPrefix + ':imgdata', (req, res)=>{
    let imgData = JSON.parse(Buffer.from(''+req.params.imgdata, 'base64'));
    let location = imgData.location;
    let message = imgData.message;
    let imgExtension = null;
    let imgNewName = null;
    let generateImgName = (str)=>{
        let tmp = str.split('.');
        imgExtension = tmp[tmp.length-1];
        //str = message + JSON.stringify(location);
        str = JSON.stringify(location); //encrypt name with location

        let x = (str.charCodeAt(0) * 719) % 1138;
        let hash = 837;
        let i;
        for (i = 1; i <= str.length; i++){
            hash = (hash * i + 5 + (str.charCodeAt(i - 1) - 64) * x) % 98503;
        }

        imgNewName = hash.toString();

        return imgNewName+'.'+imgExtension;
    };
    let storage = multer.diskStorage({
        destination: (req, file, cb)=>{cb(null, imgPath);},
        filename: (req, file, cb)=>{cb(null, generateImgName(file.originalname));}
    });
    let upload = multer({storage: storage}).single('file');

    upload(req, res, (err)=>{
        if(err) {
            res.json({error_code:1,err_desc:err});
            return;
        }
        else {
            models.images
                .create({
                    latitude: location.latitude,
                    longitude: location.longitude,
                    name: imgNewName,
                    extension: imgExtension,
                    message: message
                })
                .then((image)=>{res.json(image.get())});
        }
    });
});

module.exports = router;