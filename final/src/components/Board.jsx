import React from 'react';
import { colors, colorRGB } from './../utils';

class Board extends React.Component {

  constructor(props) {
    super(props);
    this.canvas = React.createRef();
  }

  componentDidMount() {
    const { piecesList, owners, newPiece } = this.props;
    const ctx = this.canvas.current.getContext('2d');
    this.drawBoard(ctx, owners);
    this.drawStones(ctx, piecesList, newPiece);
  }

  componentDidUpdate() {
    const { piecesList, owners, newPiece } = this.props;
    const ctx = this.canvas.current.getContext('2d');
    this.clearCanvas(ctx);
    this.drawBoard(ctx, owners);
    this.drawStones(ctx, piecesList, newPiece);
  }

  clearCanvas(ctx) {
    ctx.clearRect(0, 0, 500, 500);
  }

  drawStones(ctx, piecesList, newPiece) {
    const radius = 5;
    for (let b = 0; b < piecesList.length; b += 1) {
      const { x, y, playerInd } = piecesList[b];
      ctx.beginPath();
      ctx.arc(x, y, radius, 0, 2 * Math.PI, false);
      ctx.fillStyle = colors[playerInd];
      ctx.fill();
      ctx.lineWidth = 1;
      ctx.strokeStyle = '#003300';
      ctx.stroke();
    }
    if (newPiece) {
      const { x, y, playerInd } = newPiece;
      ctx.beginPath();
      ctx.arc(x, y, radius, 0, 2 * Math.PI, false);
      ctx.fillStyle = colors[playerInd];
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#ffcc00';
      ctx.stroke();
    }
  }

  drawBoard(ctx, owners) {
    const imageData = ctx.getImageData(0, 0, 500, 500);
    const { data } = imageData;
    for (let y = 0; y < 500; y += 1) {
      for (let x = 0; x < 500; x += 1) {
        const index = (y * 500 + x) * 4;
        const owner = owners[x][y];
        if (owner > -1) {
          const gridColor = colorRGB[owner];
          for (let i = 0; i < 3; i += 1) {
            data[index + i] = gridColor[i];
          }
          data[index + 3] = 125;
        }
      }
    }
    ctx.putImageData(imageData, 0, 0);
  }

  handleClick = (event) => {
    var rect = this.canvas.current.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    this.props.handleCanvasClick(x, y);
  }

  render() {
    return (
      <div id="canvas">
        <canvas
          height={500}
          width={500}
          ref={this.canvas}
          onClick={(e) => this.handleClick(e)}
        />
      </div>
    );
  }
}

export default Board;
