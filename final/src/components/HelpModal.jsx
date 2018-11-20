import React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';

function Transition(props) {
  return <Slide direction="up" {...props} />;
}

const HelpModal = (props) => {

  return (
    <Dialog
      open={props.open}
      onClose={props.handleClose}
      scroll="paper"
      aria-labelledby="scroll-dialog-title"
      TransitionComponent={Transition}

    >
      <DialogTitle id="scroll-dialog-title">Rules of the Game</DialogTitle>
      <DialogContent>
          <Typography variant="body1" paragraph>
          Given a set of point-sized stones (which we will call stones for simplicity) of various
          colors, a Gravitational Voronoi diagram is a tesselation of a plane into colored regions
          such that every point x has the color of the stones that give it the greatest pull.
          </Typography>

          <Typography variant="body1" paragraph>
          The pull for a color c at point x with weight w is calculated as follows:
          <br/>
          Take all the stones for color c and compute the Euclidean distances to x, say d1, d2, ... dk.
          <br/>
          Then pull(c,x) = (w/(d1*d1)) + (w/(d2*d2)) + ... + (w/(dk*dk)).
          <br/>
          It's as if we're computing the color of a point based on the color that gives
          the greatest pull.
          </Typography>

          <Typography variant="body1" paragraph>
          The Graviational Voronoi game is a n-person game that works as follows:
          <br/>
          n players can distribute their alloted weight across a max of N stones.
          <br/>
          The first player places one stone, then the second player places one stone,
          with play alternates with each player placing one stone until the
          all players have placed N stones or exhausted all their weight.
          <br/>
          Additionally, every stone must be a Euclidean distance of at least d units
          away from any other stone.
          </Typography>

          <Typography variant="body1" paragraph>
          The winner of the game is then the player with the most controlled area at the end of the game.
          </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={props.handleClose} color="primary">
          Got It!
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default HelpModal;
