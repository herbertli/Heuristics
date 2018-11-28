import React from 'react';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Button from '@material-ui/core/Button';
import TableFooter from '@material-ui/core/TableFooter';

const Scoreboard = (props) => {
  const { scores, playersList, numStones, showTotal, handleClick } = props;
  let tableRows;
  let headerRow;
  if (showTotal) {
    let total = 0;
    let totals = [];
    for (let player of playersList) {
      let playerTotal = 0;
      for (let score of player.scores) {
        playerTotal += score;
      }
      totals.push(playerTotal);
      total += playerTotal;
    }
    const percentages = playersList.map(player => {
      if (total === 0) {
        return 0;
      } else {
        let playerTotal = 0;
        for (let score of player.scores) {
          playerTotal += score;
        }
        return ((playerTotal / total) * 100).toFixed(2);
      }
    });

    const rows = playersList.map((player, i) => {
      return {
        name: player.name,
        color: player.color,
        score: totals[i],
        percentage: percentages[i],
        id: i,
      };
    });

    tableRows = rows.map(row => {
      return (
        <TableRow key={row.id} selected={row.id === props.currentPlayer}>
          <TableCell component="th" scope="row">
            {row.name}
          </TableCell>
          <TableCell numeric>{row.score}</TableCell>
          <TableCell numeric>{row.percentage}</TableCell>
          <TableCell style={{ background: row.color }}></TableCell>
        </TableRow>
      );
    });
    headerRow = (<>
      <TableCell>Name</TableCell>
      <TableCell numeric>Total Score</TableCell>
      <TableCell numeric>Total %</TableCell>
      <TableCell numeric>Color</TableCell>
    </>);
  } else {
    let total = 0;
    for (let score of scores) {
      total += score;
    }
    const percentages = scores.map(e => {
      if (total === 0) return 0;
      else return ((e / total) * 100).toFixed(2);
    });

    const rows = playersList.map((player, i) => {
      return {
        name: player.name,
        color: player.color,
        score: scores[i],
        weightRemaining: player.weightRemaining,
        stonesRemaining: numStones - player.piecesPlaced,
        percentage: percentages[i],
        id: i,
      };
    });
    tableRows = rows.map(row => {
      return (
        <TableRow key={row.id} selected={row.id === props.currentPlayer}>
          <TableCell component="th" scope="row">
            {row.name}
          </TableCell>
          <TableCell numeric>{row.score}</TableCell>
          <TableCell numeric>{row.weightRemaining}</TableCell>
          <TableCell numeric>{row.stonesRemaining}</TableCell>
          <TableCell numeric>{row.percentage}</TableCell>
          <TableCell style={{ background: row.color }}></TableCell>
        </TableRow>
      );
    });
    headerRow = (<>
      <TableCell>Name</TableCell>
      <TableCell numeric>Score</TableCell>
      <TableCell numeric>Weight Left</TableCell>
      <TableCell numeric>Stones Left</TableCell>
      <TableCell numeric>%</TableCell>
      <TableCell numeric>Color</TableCell>
    </>)
  }

  return (
    <Table padding="dense">
      <TableHead>
        <TableRow>
          {headerRow}
        </TableRow>
      </TableHead>
      <TableBody>
        {tableRows}
      </TableBody>
      <TableFooter>
        <TableRow>
          <TableCell colSpan={showTotal ? 4 : 6}>
            <Button variant="contained" color="primary" onClick={handleClick} style={{ float: 'right' }}>
              {showTotal ? 'Show Round Scores' : 'Show Total Scores'}
            </Button>
          </TableCell>
        </TableRow>
      </TableFooter>
    </Table>
  );
}

export default Scoreboard;