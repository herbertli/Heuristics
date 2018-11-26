import React, { Component } from 'react';

import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import IconButton from '@material-ui/core/IconButton';
import QuestionIcon from '@material-ui/icons/HelpOutline';

import GameOver from './components/GameOver';
import Scoreboard from './components/Scoreboard';
import Board from './components/Board';
import GameStart from './components/GameStart';
import PlayerInfo from './components/PlayerInfo';
import HelpModal from './components/HelpModal';
import WeightSelectionModal from './components/WeightSelectionModal';
import { checkValid, colors, calculateBoard } from './utils';
import './App.css';

class App extends Component {

  initialState = {
    stage: 0,
    numPlayers: 2,
    numStones: 5,
    minDist: 60.0,
    piecesList: [],
    playersList: [],
    gravPer: 1000,
    displayHelpBox: false,
    showWeightOverlay: false,
    currentPlayer: -1,
    isPlaying: false,
    newPiece: null,
  }

  constructor() {
    super();
    this.state = this.initialState;
  }

  createNewPlayer = (i) => {
    return {
      name: "Player " + (i + 1),
      color: colors[i],
      weightRemaining: this.state.gravPer,
      piecesPlaced: 0,
    }
  }

  createNewPiece = (x, y, weight, ind) => {
    return {
      x,
      y,
      weight,
      playerInd: ind
    }
  }

  handleSubmit = (newOptions) => {
    let newStage = this.state.stage;
    if (newStage === 0) {
      newStage = 1;
      let newPlayers = [];
      for (let i = 0; i < newOptions.numPlayers; i++) {
        newPlayers.push(this.createNewPlayer(i));
      }
      this.setState({
        ...newOptions,
        playersList: newPlayers,
        stage: newStage,
      });
    } else if (newStage === 1) {
      newStage = 3;
      this.setState({
        currentPlayer: 0,
        stage: newStage,
        isPlaying: true,
      });
    }
  }

  handlePlayerChange = (ind) => (event) => {
    let newPlayers = [...this.state.playersList];
    newPlayers[ind].name = event.target.value;
    this.setState({
      playersList: newPlayers,
    });
  }

  handleCanvasClick = (x, y) => {
    this.handleBoardClick(x, y, this.state.currentPlayer);
  }

  handleBoardClick = (x, y, playerInd) => {
    const { piecesList, minDist } = this.state;
    const isValid = checkValid(x, y, piecesList, minDist);
    if (!isValid) {
      this.setState({
        showError: true,
        errorMessage: "Invalid Move!"
      });
    } else {
      this.setState({
        showWeightOverlay: true,
        newPiece: { x, y, playerInd }
      });
    }
  }

  getValidPlayer = (currentPlayer, playersList) => {
    const { numPlayers, numStones } = this.state;
    for (let i = 1; i <= numPlayers; i += 1) {
      const newPlayer = (currentPlayer + 1) % numPlayers
      if (playersList[newPlayer].weightRemaining > 0 && playersList[newPlayer].piecesPlaced + 1 <= numStones) {
        return newPlayer;
      }
    }
    return -1;
  }

  handleWeightSelection = (weight) => {
    const { piecesList, newPiece, currentPlayer, playersList } = this.state;
    const { x, y, playerInd } = newPiece;
    let newPieces = [...piecesList];
    newPieces.push(this.createNewPiece(x, y, weight, playerInd));
    let newPlayers = [...playersList];
    newPlayers[playerInd].weightRemaining -= weight;
    newPlayers[playerInd].piecesPlaced += 1;
    const nextPlayer = this.getValidPlayer(currentPlayer, newPlayers);
    if (nextPlayer === -1) {
      this.setState({
        piecesList: newPieces,
        showWeightOverlay: false,
        playersList: newPlayers,
        currentPlayer: -1,
        stage: 2,
        newPiece: null,
      });
    } else {
      this.setState({
        showWeightOverlay: false,
        piecesList: newPieces,
        currentPlayer: nextPlayer,
        playersList: newPlayers,
        newPiece: null,
      });
    }
  }

  cancelWeightSelection = () => {
    this.setState({
      showWeightOverlay: false,
      newPiece: null
    });
  }

  closeHelpModal = () => {
    this.setState({ displayHelpBox: false });
  };

  renderOverlay = () => {
    const { stage, playersList } = this.state;

    switch (stage) {
      case 0:
        return <GameStart
          handleSubmit={this.handleSubmit}
        />
      case 1:
      default:
        return <PlayerInfo
          playersList={playersList}
          handleChange={this.handlePlayerChange}
          handleSubmit={this.handleSubmit}
        />
    }
  }

  resetGame = () => {
    this.setState(this.initialState);
  }

  renderGame = () => {
    const {
      piecesList,
      playersList,
      numPlayers,
      currentPlayer,
      newPiece,
      stage,
      numStones
    } = this.state;
    const { scores, owners } = calculateBoard(500, 500, piecesList, numPlayers);

    return (<>
      <Grid item xs={6}>
        <Board
          piecesList={piecesList}
          owners={owners}
          handleCanvasClick={stage !== 2 ? this.handleCanvasClick : null}
          newPiece={newPiece}
        />
      </Grid>
      <Grid item xs={6}>
        <Grid container>
          <Grid item xs={12}>
            <Scoreboard scores={scores} playersList={playersList} currentPlayer={currentPlayer} numStones={numStones} />
          </Grid>
          <Grid item xs={12}>
            { stage === 2 ? <GameOver handleClick={this.resetGame} /> : null }
          </Grid>
        </Grid>
      </Grid>
    </>);
  }

  render() {
    const {
      stage,
      showWeightOverlay,
      displayHelpBox,
      playersList,
      currentPlayer,
    } = this.state;

    return (
      <div className="App">
        <Grid container spacing={8} justify="center">
          <Grid item xs={12}>
            <Typography component="h3" variant="h3" style={{ textAlign: "center" }} gutterBottom>
              Gravitational Voronoi
              <IconButton color="primary" aria-label="Help" onClick={() => this.setState({ displayHelpBox: true})}>
                <QuestionIcon />
              </IconButton>
            </Typography>
          </Grid>
          {stage > 1 ? this.renderGame() : <Grid item xs={4}>{this.renderOverlay()}</Grid>}
        </Grid>
        <HelpModal open={displayHelpBox} handleClose={this.closeHelpModal} />
        <WeightSelectionModal
          open={showWeightOverlay}
          handleClose={this.handleWeightSelection}
          currentPlayer={playersList[currentPlayer]}
          handleCancel={this.cancelWeightSelection}
        />

      </div>
    );
  }
}

export default App;
