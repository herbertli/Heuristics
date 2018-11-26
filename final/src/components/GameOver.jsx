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

class GameOver extends React.Component {

  saveBoard = () => {
    const link = document.createElement('a');
    const canvas = document.getElementById("physCanvas");
    link.href = canvas.toDataURL();
    link.download = "board.png";
    link.click();
  }

  render() {
    const { handleClick, classes } = this.props;
    return (
      <Grid container justify="center" className={classes.grid} >
        <Grid item xs={12}>
          <Typography variant="h5" gutterBottom style={{ textAlign: "center" }}>
            Game Over!
      </Typography>
        </Grid>
        <Grid item>
          <Button variant="contained" color="primary" className={classes.button} onClick={handleClick}>
            Restart!
        </Button>
        </Grid>
        <Grid item>
          <Button variant="contained" color="primary" className={classes.button} onClick={this.saveBoard}>
            Save Board
      </Button>
        </Grid>
      </Grid>
    );
  }
}

export default withStyles(styles)(GameOver);