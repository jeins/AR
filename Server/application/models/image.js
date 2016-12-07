'use strict';

module.exports = (sequelize, DataTypes)=>{
    let image = sequelize.define('images', {
        latitude: DataTypes.DOUBLE,
        longitude: DataTypes.DOUBLE,
        name: DataTypes.STRING,
        extension: DataTypes.STRING,
        message: DataTypes.TEXT,
        createdAt: DataTypes.DATE,
        updatedAt: DataTypes.DATE
    });

    return image;
};