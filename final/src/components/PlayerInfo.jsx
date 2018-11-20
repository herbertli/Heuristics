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

const PlayerInfo = (props) => {

  const { playersList, classes } = props;
  return <Paper elevation={3} className={classes.root}>
    <Typography variant="h4" component="h3">
      Player Info:
    </Typography>
    <Grid container direction="column" justify="center" alignItems="center" spacing={8}>
      {playersList.map((player, i) =>
        <Grid item key={"playerInfo" + i}>
          <TextField
            label="Player Name:"
            value={player.name}
            onChange={props.handleChange(i)}
            margin="normal"
            variant="outlined"
          />
        </Grid>
      )}
      <Grid item>
        <Button variant="contained" color="primary" onClick={props.handleSubmit}>
          Submit
        </Button>
      </Grid>
    </Grid>
  </Paper>
}

export default withStyles(styles)(PlayerInfo);