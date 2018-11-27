import React, { Component } from 'react';

import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import IconButton from '@material-ui/core/IconButton';
import QuestionIcon from '@material-ui/icons/HelpOutline';
import Snackbar from '@material-ui/core/Snackbar';

import CustomSnackbar from './components/CustomSnackbar';
import GameOver from './components/GameOver';
import RoundOver from './components/RoundOver'
import Scoreboard from './components/Scoreboard';
import Board from './components/Board';
import GameStart from './components/GameStart';
import PlayerInfo from './components/PlayerInfo';
import HelpModal from './components/HelpModal';
import WeightSelectionModal from './components/WeightSelectionModal';
import { checkValid, colors, calculateBoard } from './utils';

class App extends Component {

  initialState = {
    stage: 'GAME_SETTINGS',
    numPlayers: 2,
    numStones: 5,
    minDist: 60.0,
    playersList: [],
    gravPer: 1000,
    isPlaying: false,
    roundNum: 0,
  }

  initialRound = {
    piecesList: [],
    displayHelpBox: false,
    showWeightOverlay: false,
    currentPlayer: 0,
    newPiece: null,
    snackOpen: false,
  }

  constructor() {
    super();
    this.state = {
      ...this.initialRound,
      ...this.initialState
    };
  }

  createNewPlayer = (i, gravPer) => {
    return {
      name: "Player " + (i + 1),
      color: colors[i],
      weightRemaining: gravPer,
      piecesPlaced: 0,
      scores: [],
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
    if (newStage === 'GAME_SETTINGS') {
      newStage = 'PLAYER_LIST';
      let newPlayers = [];
      for (let i = 0; i < newOptions.numPlayers; i++) {
        newPlayers.push(this.createNewPlayer(i, newOptions.gravPer));
      }
      this.setState({
        ...newOptions,
        playersList: newPlayers,
        stage: newStage,
      });
    } else if (newStage === 'PLAYER_LIST') {
      newStage = 'PLAYING_ROUND';
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
        snackOpen: true,
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
      const newPlayer = (currentPlayer + i) % numPlayers
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
        stage: 'ROUND_OVER',
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
    console.log("canceled, should remove new piece");
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
      case 'GAME_SETTINGS':
        return <GameStart
          handleSubmit={this.handleSubmit}
        />
      case 'PLAYER_LIST':
      default:
        return <PlayerInfo
          playersList={playersList}
          handleChange={this.handlePlayerChange}
          handleSubmit={this.handleSubmit}
        />
    }
  }

  resetGame = () => {
    this.setState({
      ...this.initialState,
      ...this.initialRound,
    });
  }

  resetRound = (scores) => {
    const { playersList, gravPer, numPlayers, roundNum } = this.state;
    let newPlayers = [...playersList];
    for (let i = 0; i < newPlayers.length; i += 1) {
      let player = newPlayers[i];
      player.piecesPlaced = 0;
      player.weightRemaining = gravPer;
      player.scores.push(scores[i]);
    }
    let newStage = 'PLAYING_ROUND';
    if (roundNum + 1 === numPlayers) {
      newStage = 'GAME_OVER'
      this.setState({
        stage: newStage,
        playersList: newPlayers,
        roundNum: roundNum + 1,
        currentPlayer: roundNum + 1,
      });
    } else {
      this.setState({
        ...this.initialRound,
        stage: newStage,
        playersList: newPlayers,
        roundNum: roundNum + 1,
        currentPlayer: roundNum + 1,
      });
    }
  }

  handleSnackClose = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }

    this.setState({ snackOpen: false });
  };


  renderGame = () => {
    const {
      piecesList,
      playersList,
      numPlayers,
      currentPlayer,
      newPiece,
      stage,
      numStones,
      minDist,
      roundNum,
    } = this.state;
    const { scores, owners } = calculateBoard(500, 500, piecesList, numPlayers);

    return (<>
      <Grid item xs={12} sm={12} md={6}>
        <Board
          piecesList={piecesList}
          owners={owners}
          handleCanvasClick={stage === 'PLAYING_ROUND' ? this.handleCanvasClick : null}
          newPiece={newPiece}
          currentPlayer={currentPlayer}
          minDist={minDist}
        />
      </Grid>
      <Grid item xs={12} sm={12} md={6}>
        <Grid container>
          <Grid item xs={12}>
            { stage === 'GAME_OVER' ? <GameOver handleClick={this.resetGame} playersList={playersList} lastScores={scores} /> :
            <Scoreboard scores={scores} playersList={playersList} currentPlayer={currentPlayer} numStones={numStones} /> }
          </Grid>
          <Grid item xs={12}>
            { stage === 'ROUND_OVER' ? <RoundOver numPlayers={numPlayers} roundNum={roundNum + 1} handleClick={() => this.resetRound(scores)} /> : null }
          </Grid>
        </Grid>
      </Grid>
    </>);
  }

  renderScene(stage) {
    if (stage === 'GAME_SETTINGS') {
      return (<Grid item xs={10} sm={6} md={4}>{this.renderOverlay()}</Grid>);
    } else if (stage === 'PLAYER_LIST') {
      return (<Grid item xs={10} sm={6} md={4}>{this.renderOverlay()}</Grid>);
    } else {
      return this.renderGame();
    }
  }

  render() {
    const {
      stage,
      showWeightOverlay,
      displayHelpBox,
      playersList,
      currentPlayer,
      minDist,
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
          {this.renderScene(stage)}
        </Grid>
        <HelpModal open={displayHelpBox} handleClose={this.closeHelpModal} />
        <WeightSelectionModal
          open={showWeightOverlay}
          handleClose={this.handleWeightSelection}
          currentPlayer={playersList[currentPlayer]}
          handleCancel={this.cancelWeightSelection}
        />
        <Snackbar
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left',
          }}
          open={this.state.snackOpen}
          autoHideDuration={3000}
          onClose={this.handleSnackClose}
        >
          <CustomSnackbar
            onClose={this.handleClose}
            variant="error"
            message={`Stones must be at least ${minDist} units apart!`}
          />
        </Snackbar>
      </div>
    );
  }
}

export default App;
