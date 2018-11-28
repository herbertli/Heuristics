import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import { checkValid, colors, colorRGB } from './../utils';


const styles = () => ({
  parentDiv: {
    position: 'relative',
    width: '500px',
    height: '500px',
    margin: 'auto',
  },
  hCanvas: {
    border: "1px solid hsl(0, 0%, 0%)",
    position: "absolute",
    top: '0px',
    left: '0px',
    zIndex: 1
  },
  pCanvas: {
    border: "1px solid hsl(0, 0%, 0%)",
    position: "relative",
    zIndex: 0
  }
});

class Board extends React.Component {

  constructor(props) {
    super(props);
    this.canvas = React.createRef();
    this.hoverCanvas = React.createRef();

  }

  componentDidMount() {
    const { piecesList, owners, newPiece } = this.props;
    const ctx = this.canvas.current.getContext('2d');
    this.clearCanvas(ctx);
    this.drawBoard(ctx, owners);
    this.drawStones(ctx, piecesList, newPiece);
  }

  componentDidUpdate() {
    const { piecesList, owners, newPiece } = this.props;
    const ctx = this.canvas.current.getContext('2d');
    const htx = this.hoverCanvas.current.getContext('2d');
    this.clearCanvas(ctx);
    this.clearCanvas(htx);
    this.drawBoard(ctx, owners);
    this.drawStones(ctx, piecesList, newPiece);
  }

  shouldComponentUpdate(nextProps) {
    if (nextProps.piecesList !== this.props.piecesList) {
      return true;
    } else {
      return false;
    }
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
    if (this.props.handleCanvasClick) this.props.handleCanvasClick(x, y);
  }

  handleHover = (event) => {
    if (!this.props.handleCanvasClick) return false;
    const { currentPlayer, piecesList, minDist } = this.props;

    const ctx = this.hoverCanvas.current.getContext('2d');
    this.clearCanvas(ctx);

    var rect = this.hoverCanvas.current.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const isValid = checkValid(x, y, piecesList, minDist);
    const r = colorRGB[currentPlayer][0];
    const g = colorRGB[currentPlayer][1];
    const b = colorRGB[currentPlayer][2];
    ctx.beginPath();
    ctx.arc(x, y, 5, 0, 2 * Math.PI, false);
    if (isValid) {
      ctx.fillStyle = `rgb(${r}, ${g}, ${b}, 1)`;
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#ffcc00';
      ctx.stroke();
    }
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.parentDiv}>
        <canvas
          id="physCanvas"
          className={classes.pCanvas}
          height={500}
          width={500}
          ref={this.canvas}
        />
        <canvas
          className={classes.hCanvas}
          height={500}
          width={500}
          ref={this.hoverCanvas}
          onClick={(e) => this.handleClick(e)}
          onMouseMove={(e) => this.handleHover(e)}
        />
      </div>
    );
  }
}

export default withStyles(styles)(Board);
