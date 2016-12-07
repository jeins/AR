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

router.get('/api/version', (req, res)=>{
    res.json({version: '1.0.0'});
});

router.get(imageApiPrefix + ':name/:extension', (req, res)=>{
    let imgExtension = req.params.extension;
    let imgName = req.params.name + '.' + imgExtension;

    fs.readFile(imgPath + imgName, (err, data)=>{
        if(err) throw err;

        res.writeHead(200, {'Content-Type': imgExtension});
        res.end(data, 'binary');
    })
});

router.post(imageApiPrefix + 'list', (req, res)=>{
    let filter = {
        latitude: req.body.location.latitude,
        longitude: req.body.location.longitude
    };

    models.images
        .findAll({
            where: filter
        })
        .then((images)=>{
            res.json(images);
        })
});

router.post(imageApiPrefix + ':imgdata', (req, res)=>{
    let imgData = JSON.parse(Buffer.from(''+req.params.imgdata, 'base64'));
    let location = imgData.location;
    let message = imgData.message;
    let imgExtension = null;
    let imgNewName = null;
    let generateImgName = (str)=>{
        let tmp = str.split('.');
        imgExtension = tmp[tmp.length-1];
        str = message + JSON.stringify(location);

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