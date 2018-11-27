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
          <Typography variant="h5" paragraph>
            Overview:
          </Typography>
          <Typography variant="body1" paragraph>
          Given a set of point-sized stones of various colors,
          a Gravitational Voronoi diagram is a tesselation of a plane into colored regions
          such that every integer point (x, y) has the color of the stones that
          give it the greatest pull.
          </Typography>

          <Typography variant="h5" paragraph>
            Pull Calculation:
          </Typography>
          <Typography variant="body1" paragraph>
          The pull for a color c at point p with coordinates (x, y) is calculated as follows:
          <br/>
          Supposing that color c has k stones placed:
          <br/>
          First, take all k stones and compute their Euclidean distances to point p
          say d1, d2, ... dk.
          <br/>
          Second, take the weights of all k stones w1, ..., wk
          <br/>
          Then pull(c, p) = (w/(d1*d1)) + (w/(d2*d2)) + ... + (w/(dk*dk)).
          <br/>
          It's as if we're computing the color of a point based on the color that gives
          the greatest pull.
          </Typography>

          <Typography variant="h5" paragraph>
            Game Flow:
          </Typography>
          <Typography variant="body1" paragraph>
          The Graviational Voronoi game is a n-person game that works as follows:
          <br/>
          n players can distribute their allotted weight across a maximum of N stones on a 500x500 board.
          <br/>
          The first player places one stone, then the second player places one stone and so on...
          <br/>
          Each player places one stone until all players have placed N stones or exhausted all their weight.
          <br/>
          Additionally, every stone must be a Euclidean distance of at least d units
          away from any other stone.
          </Typography>

          <Typography variant="body1" paragraph>
          The winner of a round is then the player with the most controlled area at the end of the round.
          </Typography>

          <Typography variant="h5" paragraph>
            Ending the Game:
          </Typography>
          <Typography variant="body1" paragraph>
          If there are n players, the game runs for a total of n rounds, allowing each player to go first.
          <br/>
          At the end of the game (after n rounds), each players' scores over every round are summed.
          <br/>
          The player with the highest combined score over the n rounds is declared the winner!
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
