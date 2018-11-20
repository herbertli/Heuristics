import React from 'react';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    ...theme.mixins.gutters(),
    paddingTop: theme.spacing.unit * 2,
    paddingBottom: theme.spacing.unit * 2,
    textAlign: "center",
  },
});

class GameStart extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      numPlayers: 2,
      numStones: 1,
      gravPer: 1000,
      minDist: 60,
      showErrorPlayers: false,
      showErrorStones: false,
      showErrorGrav: false,
      showErrorDist: false,
    }
  }

  handleChange = name => event => {
    this.setState({
      [name]: event.target.value,
    });
  }

  handleSubmit = () => {
    const { showErrorPlayers, showErrorStones, showErrorGrav, showErrorDist } = this.validateState();
    if (showErrorPlayers || showErrorStones || showErrorGrav || showErrorDist) {
      this.setState({
        showErrorPlayers, showErrorStones, showErrorGrav, showErrorDist
      });
    } else {
      let { numPlayers, numStones, gravPer, minDist } = this.state;
      numPlayers = parseInt(numPlayers);
      numStones = parseInt(numStones);
      gravPer = parseInt(gravPer);
      minDist = parseInt(minDist);
      const newOptions = {
        numPlayers, numStones, gravPer, minDist
      }
      this.props.handleSubmit(newOptions);
    }
  }

  validateState = () => {
    const { numPlayers, numStones, gravPer, minDist } = this.state;
    const nNum = parseInt(numPlayers);
    const nStone = parseInt(numStones);
    const nGrav = parseInt(gravPer);
    const nDist = parseInt(minDist);
    const showErrorPlayers = isNaN(nNum) || !Number.isInteger(nNum);
    const showErrorStones = isNaN(nStone) || !Number.isInteger(nStone);
    const showErrorGrav = isNaN(nGrav) || !Number.isInteger(nGrav);
    const showErrorDist = isNaN(nDist);
    return {
      showErrorPlayers, showErrorStones, showErrorGrav, showErrorDist
    };
  }

  render() {
    const { numPlayers, gravPer, numStones, minDist, showErrorDist, showErrorPlayers, showErrorGrav, showErrorStones } = this.state;
    const { classes } = this.props;

    return <Paper className={classes.root} elevation={3}>
      <Typography variant="h4" component="h3">
        Game Options
      </Typography>
      <Grid container direction="column" justify="center" alignItems="center" spacing={8}>
        <Grid item>
          <TextField
            label="Number Of Players"
            value={numPlayers}
            onChange={this.handleChange("numPlayers")}
            margin="normal"
            variant="outlined"
            error={showErrorPlayers}
          />
        </Grid>
        <Grid item>
          <TextField
            label="Available Gravity Per Player"
            value={gravPer}
            onChange={this.handleChange("gravPer")}
            margin="normal"
            variant="outlined"
            error={showErrorGrav}
          />
        </Grid>
        <Grid item>
          <TextField
            label="Max Stones Per Player"
            value={numStones}
            onChange={this.handleChange("numStones")}
            margin="normal"
            variant="outlined"
            error={showErrorStones}
          />
        </Grid>
        <Grid item>
          <TextField
            label="Minimum Distance"
            value={minDist}
            onChange={this.handleChange("minDist")}
            margin="normal"
            variant="outlined"
            error={showErrorDist}
          />
        </Grid>
        <Grid item>
          <Button variant="contained" color="primary" onClick={() => this.handleSubmit()}>
            Submit
          </Button>
        </Grid>
      </Grid>
    </Paper>
  }
}

export default withStyles(styles)(GameStart);