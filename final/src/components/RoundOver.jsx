import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';

const styles = theme => ({
  button: {
    margin: theme.spacing.unit,
  },
  grid: {
    margin: theme.spacing.unit,
  }
});

class RoundOver extends React.Component {

  saveBoard = () => {
    const link = document.createElement('a');
    const canvas = document.getElementById("physCanvas");
    link.href = canvas.toDataURL();
    link.download = "board.png";
    link.click();
  }

  render() {
    const { handleClick, classes, roundNum, numPlayers } = this.props;
    return (
      <Grid container justify="center" className={classes.grid}>
        <Grid item xs={12}>
          <Typography variant="h5" gutterBottom style={{ textAlign: "center" }}>
            Round {roundNum} Over!
      </Typography>
        </Grid>
        {roundNum + 1 <= numPlayers ?
        <Grid item>
          <Button variant="contained" color="primary" className={classes.button} onClick={handleClick}>
            Next Round
        </Button>
        </Grid> : null }
        {roundNum + 1 > numPlayers ?
        <Grid item>
          <Button variant="contained" color="primary" className={classes.button} onClick={handleClick}>
            Next
        </Button>
        </Grid> : null }
        <Grid item>
          <Button variant="contained" color="primary" className={classes.button} onClick={this.saveBoard}>
            Save Board
      </Button>
        </Grid>
      </Grid>
    );
  }
}

export default withStyles(styles)(RoundOver);