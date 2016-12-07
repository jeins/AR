'use strict';

module.exports = {
  up: function (queryInterface, Sequelize) {
    /*
      Add altering commands here.
      Return a promise to correctly handle asynchronicity.

      Example:
      return queryInterface.createTable('users', { id: Sequelize.INTEGER });
    */
      queryInterface.createTable('images', {
          id: {type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true},
          latitude: {type: Sequelize.DOUBLE},
          longitude: {type: Sequelize.DOUBLE},
          name: {type: Sequelize.STRING},
          extension: {type: Sequelize.STRING},
          message: {type: Sequelize.TEXT},
          createdAt: {type: Sequelize.DATE},
          updatedAt: {type: Sequelize.DATE}
      });
  },

  down: function (queryInterface, Sequelize) {
    /*
      Add reverting commands here.
      Return a promise to correctly handle asynchronicity.

      Example:
      return queryInterface.dropTable('users');
    */
  }
};
