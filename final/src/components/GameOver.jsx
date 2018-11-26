import React from 'react';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';

const GameOver = (props) => {

  return (<>
    <Typography variant="h5" gutterBottom>
      Game Over!
      <Button variant="contained" color="primary" onClick={props.handleClick}>
        Restart!
      </Button>
    </Typography>
  </>);
}

export default GameOver;