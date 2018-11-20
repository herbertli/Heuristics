import React from 'react';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

const Scoreboard = (props) => {
  const { scores, playersList } = props;
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
      percentage: percentages[i],
      id: i,
    };
  });

  return (
    <Table>
      <TableHead>
        <TableRow>
          <TableCell>Name</TableCell>
          <TableCell numeric>Score</TableCell>
          <TableCell numeric>Remaining</TableCell>
          <TableCell numeric>%</TableCell>
          <TableCell numeric>Color</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {rows.map(row => {
          return (
            <TableRow key={row.id} selected={row.id === props.currentPlayer}>
              <TableCell component="th" scope="row">
                {row.name}
              </TableCell>
              <TableCell numeric>{row.score}</TableCell>
              <TableCell numeric>{row.weightRemaining}</TableCell>
              <TableCell numeric>{row.percentage}</TableCell>
              <TableCell style={{ background: row.color }}></TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}

export default Scoreboard;