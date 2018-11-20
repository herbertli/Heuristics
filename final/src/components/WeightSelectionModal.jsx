import React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import TextField from '@material-ui/core/TextField';

class WeightModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedWeight: 0,
    }
  }

  handleChange = name => event => {
    this.setState({
      [name]: event.target.value,
    });
  };

  render() {
    return (
      <Dialog
        open={this.props.open}
        scroll="paper"
        aria-labelledby="weight-dialog-title"
      >
        <DialogTitle id="weight-dialog-title">Weight Selection</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Please Select a Weight
          </DialogContentText>
          <TextField
            label="Weight"
            onChange={this.handleChange('selectedWeight')}
            margin="normal"
            variant="outlined"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={this.props.handleCancel} color="primary">
            Cancel
        </Button>
          <Button onClick={() => this.props.handleClose(this.state.selectedWeight)} color="primary">
            Submit
        </Button>
        </DialogActions>
      </Dialog>
    );
  }
}
export default WeightModal;
