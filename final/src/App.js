import React, { Component } from 'react';
import PlaceHolder from './components/Placeholder';

class App extends Component {

  constructor() {
    super();
    this.state = {
      stage: 0,
      numPlayers: null,
      piecesList: [],
      playersList: [],
    }
  }

  render() {
    const { stage, piecesList, playersList } = this.state;
    return (
      <div className="App">
        Gravitational Voronoi
        <PlaceHolder text="board" piecesList={piecesList} />
        <PlaceHolder text="scoreboard" playersList={playersList} />
        { stage === 0 ? <PlaceHolder text="game start overlay" /> : null }
        { stage === 1 ? <PlaceHolder text="player info overlay" /> : null }
        { stage === 2 ? <PlaceHolder text="game info overlay" /> : null }
        { stage === 3 ? <PlaceHolder text="gravity selection overlay" /> : null }
        { stage === 4 ? <PlaceHolder text="game over overlay" /> : null }
      </div>
    );
  }
}

export default App;
