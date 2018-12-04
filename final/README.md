# Weighted Voronoi

## Description:

Gravitational Voronoi but with weighted pieces

## Authors:

Botty McBotFace (Herbert and Aldo)

## Architecture:

- React
- Material-UI
- HTML5 Canvas

## Building:

Make sure you have either `yarn` or `npm` installed

Build the project using either:
    * `yarn build`
    * `npm run build`

## Deploying to Dr. Ecco:

### 1. Setup:
1. Change the homepage in package.json to be the base url of the server
    * e.g. https://cims.nyu.edu/drecco2016/games/weighted_voronoi

### 2a. If you have rsync:
1. Copy an Empty Template to root directory (it should be in the same directory as package.json)
2. `yarn submit`
3. Deploy!

### 2b. Or do it the normal way:
1. Copy an Empty Template to root directory (it should be in the same directory as package.json)
2. Drag contents of build/ folder (only the contents, not the folder itself) into an Empty Template
3. Make the following changes:
    * Change index.html to iframe.html
4. Deploy!

Note that using this method of deployment, you will need to embed this game as an `<iframe>` on your webpage.

## Implementation Details:

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

Make sure you have some understanding of React and HTML5 Canvas before continuing...

Folder Structure:
- public/
   - images/ - Images used in Help pop-up
   - index.html - Main page
- src/
   - App.js - Main entry point (look here first!!!)
   - components/ - Various UI components

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.<br>
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.<br>
You will also see any lint errors in the console.

### `npm test`

Launches the test runner in the interactive watch mode.<br>
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `npm run build`

Builds the app for production to the `build` folder.<br>
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.<br>
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).
