import React, { Component } from 'react';

import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import IconButton from '@material-ui/core/IconButton';
import QuestionIcon from '@material-ui/icons/HelpOutline';

import PlaceHolder from './components/Placeholder';
import Scoreboard from './components/Scoreboard';
import Board from './components/Board';
import GameStart from './components/GameStart';
import PlayerInfo from './components/PlayerInfo';
import HelpModal from './components/HelpModal';
import WeightSelectionModal from './components/WeightSelectionModal';
import { checkValid, colors, calculateBoard } from './utils';
import './App.css';

class App extends Component {

  constructor() {
    super();
    this.state = {
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
    }
  }

  handleInfoChange = name => event => {
    this.setState({
      [name]: event.target.value,
    });
  }

  createNewPlayer = (i) => {
    return {
      name: "Player " + i,
      color: colors[i],
      weightRemaining: this.state.gravPer,
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

  handleSubmit = () => {
    let newStage = this.state.stage;
    if (newStage === 0) {
      newStage = 1;
      let newPlayers = [];
      for (let i = 0; i < this.state.numPlayers; i++) {
        newPlayers.push(this.createNewPlayer(i));
      }
      this.setState({
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

  handleWeightSelection = (weight) => {
    const { piecesList, newPiece, currentPlayer, numPlayers, playersList } = this.state;
    const { x, y, playerInd } = newPiece;
    let newPieces = [...piecesList];
    newPieces.push(this.createNewPiece(x, y, weight, playerInd));
    let newPlayers = [...playersList];
    newPlayers[playerInd].weightRemaining -= weight;
    this.setState({
      showWeightOverlay: false,
      piecesList: newPieces,
      currentPlayer: (currentPlayer + 1) % numPlayers,
      playersList: newPlayers,
    });
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
    const {
      stage,
      gravPer,
      numPlayers,
      numStones,
      playersList,
      minDist
    } = this.state;

    switch (stage) {
      case 0:
        return <GameStart
          numPlayers={numPlayers}
          gravPer={gravPer}
          handleChange={this.handleInfoChange}
          handleSubmit={this.handleSubmit}
          numStones={numStones}
          minDist={minDist}
        />
      case 1:
        return <PlayerInfo
          playersList={playersList}
          handleChange={this.handlePlayerChange}
          handleSubmit={this.handleSubmit}
        />
      case 2:
      default:
        return <PlaceHolder text="game over overlay" />;
    }
  }

  renderGame = () => {
    const {
      piecesList,
      playersList,
      numPlayers,
      currentPlayer,
    } = this.state;
    const { scores, owners } = calculateBoard(500, 500, piecesList, numPlayers);

    return (<>
      <Grid item xs={7}>
        <Board
          piecesList={piecesList}
          owners={owners}
          handleCanvasClick={this.handleCanvasClick}
        />
      </Grid>
      <Grid item xs={5}>
        <Scoreboard scores={scores} playersList={playersList} currentPlayer={currentPlayer} />
      </Grid>
    </>);
  }

  render() {
    const {
      stage,
      showWeightOverlay,
      displayHelpBox,
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
          {stage > 2 ? this.renderGame() : <Grid item xs={4}>{this.renderOverlay()}</Grid>}
        </Grid>
        <HelpModal open={displayHelpBox} handleClose={this.closeHelpModal} />
        <WeightSelectionModal
          open={showWeightOverlay}
          handleClose={this.handleWeightSelection}
          handleCancel={this.cancelWeightSelection}
        />

      </div>
    );
  }
}

export default App;
