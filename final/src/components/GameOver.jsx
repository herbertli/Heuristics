import React from 'react';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';

const GameOver = (props) => {

  return (<>
    <Typography variant="h5" gutterBottom>
      Game Over!
        </Typography>
    <Button variant="contained" color="primary" onClick={props.handleClick}>
      Restart!
      </Button>
  </>);
}

export default GameOver;