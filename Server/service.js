'use strict';

let http = require('http'),
    express = require('express'),
    bodyParser = require('body-parser'),
    morgan = require('morgan'),

    corsMiddleware = require('./application/CorsMiddleware'),
    imageRouter = require('./application/ImageRouter')
    ;

let app = express();

app.server = http.createServer(app);

app.use(morgan('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(corsMiddleware);
app.use('/', imageRouter);

let host = 'localhost',
    port = 8888;
app.server.listen(port, host, ()=>{
    console.log("Server is running on %s:%s", host, port);
});