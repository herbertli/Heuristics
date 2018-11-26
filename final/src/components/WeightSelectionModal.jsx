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
      selectedWeight: null,
      showErrorWeight: false,
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.currentPlayer !== prevProps.currentPlayer) {
      this.setState({
        selectedWeight: null,
        showErrorWeight: false,
      });
    }
  }

  handleChange = name => event => {
    const { showErrorWeight } = this.validateState(event.target.value);
    this.setState({
      showErrorWeight: showErrorWeight,
      [name]: event.target.value,
    });
  };

  validateState = (selectedWeight) => {
    if (!selectedWeight)
      selectedWeight = this.state.selectedWeight;
    const { weightRemaining } = this.props.currentPlayer;
    const nWeight = parseInt(selectedWeight);
    const showErrorWeight = isNaN(nWeight) || !Number.isInteger(nWeight) || nWeight > weightRemaining || nWeight <= 0;
    return {
      showErrorWeight
    };
  }

  handleSubmit = () => {
    const { showErrorWeight } = this.validateState();
    if (showErrorWeight) {
      this.setState({
        showErrorWeight
      });
    } else {
      let { selectedWeight } = this.state;
      selectedWeight = parseInt(selectedWeight);
      this.props.handleClose(selectedWeight)
    }
  }

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
            error={this.state.showErrorWeight}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={this.props.handleCancel} color="primary">
            Cancel
          </Button>
          <Button onClick={() => this.handleSubmit()} color="primary">
            Submit
          </Button>
        </DialogActions>
      </Dialog>
    );
  }
}
export default WeightModal;
