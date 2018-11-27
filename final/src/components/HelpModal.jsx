import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogContent from '@material-ui/core/DialogContent';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';

function Transition(props) {
  return <Slide direction="up" {...props} />;
}

const styles = theme => ({
  root: {
    ...theme.mixins.gutters(),
    marginTop: theme.spacing.unit,
  },
  screenImg: {
    width: '50%',
    margin: '0 auto',
    display: 'block',
  },
  gameoverImg: {
    width: '80%',
    margin: '0 auto',
    display: 'block',
  }
});

const Text = (props) => <Typography variant="body1" {...props}>{props.children}</Typography>
const LText = (props) => <li><Text {...props}>{props.children}</Text></li>

const HelpModal = (props) => {

  const { classes } = props;

  return (
    <Dialog
      open={props.open}
      onClose={props.handleClose}
      scroll="paper"
      aria-labelledby="scroll-dialog-title"
      TransitionComponent={Transition}
    >
      <DialogContent>
        <Grid container justify="center" direction="row" spacing={16} className={classes.root}>
          <Grid item>
            <Text variant="h4" paragraph>Rules of the Game</Text>
          </Grid>
          <Grid item>
            <Text variant="h5" paragraph>Overview:</Text>
            <hr/>
            <Text paragraph>
              Given a set of point-sized stones of various colors,
              a Gravitational Voronoi diagram is a tesselation of a plane into colored regions
              such that every point with integer coordinates (x, y) has the color of the stones that
              give it the greatest pull.
            </Text>
          </Grid>
          <Grid item>
            <img src="images/board.png" alt="board" className={classes.screenImg}/>
          </Grid>
          <Grid item>
            <Text variant="h5" paragraph>Playing a Round:</Text>
            <hr/>
            <Text paragraph>
              Graviational Voronoi is a n player game that works as follows:
            </Text>
            <ul>
              <LText>
                n players are each assigned a color, and are each allocated W units of weight
              </LText>
              <LText>
                Each player can distribute their allotted weight across a maximum of S stones on a 500x500 board.
              </LText>
              <LText>
                The first player places one stone, then the second player places one stone and so on...
              </LText>
              <LText>
                Each player places one stone until all players have placed S stones or have exhausted all their weight.
              </LText>
              <LText>
                If a player cannot place a stone (either they have used all of their weight or placed all of their stones),
                their turn is skipped
              </LText>
              <LText>
                Additionally, every stone must be a Euclidean distance of at least d units
                away from any other stone.
              </LText>
            </ul>
            <Text>n, S, W, and d are set at the beginning of every game.</Text>
            <Text paragraph>
              The winner of a round is the player with the most controlled area at the end of the round.
            </Text>
          </Grid>
          <Grid item>
            <img src="images/options.png" alt="options" className={classes.screenImg}/>
          </Grid>
          <Grid item>
            <Text variant="h5" paragraph>Ending a Game:</Text>
            <hr/>
            <ul>
              <LText>
                If there are n players, the game runs for a total of n rounds, allowing each player to go first.
              </LText>
              <LText>
                At the end of the game (after n rounds), each players' scores over all rounds are summed.
              </LText>
              <LText>
                The player with the highest combined score over all n rounds is declared the winner!
              </LText>
            </ul>
          </Grid>
          <Grid item>
            <img src="images/gameover.png" alt="gameover" className={classes.gameoverImg}/>
          </Grid>
          <Grid item>
            <Text variant="h5" paragraph>Pull Calculation:</Text>
            <hr/>
            <Text paragraph>
              The pull for a color c at point p with coordinates (x, y) is calculated as follows:
            </Text>
            <Text paragraph>Supposing that color c has k stones placed:</Text>
            <ul>
              <LText>
                Take all k stones and compute their Euclidean distances to point p
                say d1, d2, ... dk.
              </LText>
              <LText>
                Take the weights of all k stones w1, ..., wk
              </LText>
              <LText>
                pull(c, p) = (w/(d1*d1)) + (w/(d2*d2)) + ... + (w/(dk*dk)).
              </LText>
            </ul>
            <Text paragraph>
              It's as if we're computing the color of a point based on the color that gives
              the greatest pull.
            </Text>
          </Grid>
          <Grid item>
            <Button onClick={props.handleClose} color="primary" variant="contained">
              Got It!
            </Button>
          </Grid>
        </Grid>
      </DialogContent>
    </Dialog>
  );
}

export default withStyles(styles)(HelpModal);
