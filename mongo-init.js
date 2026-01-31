// MongoDB initialization script for Famistry
// This script runs when the MongoDB container starts for the first time

// Switch to the famistry database
db = db.getSiblingDB('famistry');

// Create the application user with permissions on the famistry database
db.createUser({
  user: 'famistry',
  pwd: 'famistry123',
  roles: [
    {
      role: 'readWrite',
      db: 'famistry'
    }
  ]
});

// Create initial collections if needed (optional - Spring Data MongoDB will create them automatically)
// db.createCollection('people');
// db.createCollection('relationships');

// Create indexes for better performance (optional - Spring Data MongoDB will create them automatically)
// db.people.createIndex({ "name": 1 });
// db.people.createIndex({ "fatherId": 1 });
// db.people.createIndex({ "motherId": 1 });
// db.people.createIndex({ "spouseId": 1 });

// Log initialization
print('MongoDB initialized successfully for Famistry!');
print('Database: famistry');
print('User: famistry');
print('Collections will be created automatically by Spring Data MongoDB');
