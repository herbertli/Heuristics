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

const GameStart = (props) => {

  const { classes } = props;

  return <Paper className={classes.root} elevation={3}>
    <Typography variant="h4" component="h3">
      Game Options
      </Typography>
    <Grid container direction="column" justify="center" alignItems="center" spacing={8}>
      <Grid item>
        <TextField
          label="Number Of Players"
          value={props.numPlayers}
          onChange={props.handleChange('numPlayers')}
          margin="normal"
          variant="outlined"
        />
      </Grid>
      <Grid item>
        <TextField
          label="Available Gravity Per Player"
          value={props.gravPer}
          onChange={props.handleChange('gravPer')}
          margin="normal"
          variant="outlined"
        />
      </Grid>
      <Grid item>
        <TextField
          label="Max Stones Per Player"
          value={props.numStones}
          onChange={props.handleChange('numStones')}
          margin="normal"
          variant="outlined"
        />
      </Grid>
      <Grid item>
        <TextField
          label="Minimum Distance"
          value={props.minDist}
          onChange={props.handleChange('minDist')}
          margin="normal"
          variant="outlined"
        />
      </Grid>
      <Grid item>
        <Button variant="contained" color="primary" onClick={props.handleSubmit}>
          Submit
          </Button>
      </Grid>
    </Grid>
  </Paper>
}

export default withStyles(styles)(GameStart);