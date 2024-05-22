import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Button from '@mui/material/Button';
import Title from './Title';
import { styled } from '@mui/material/styles';
import { useMarketData } from '../context/MarketData';

// Generate Position Data
function createData(id, symbol, size, entryPrice, margin, leverage, position) {
  return { id, symbol, size, entryPrice, margin, leverage, position };
}

const rows = [
  createData(0, 'BTC', '1.5 BTC', 45000, 5000, 10, 'Long'),
  createData(1, 'ETH', '10 ETH', 3000, 3000, 20, 'Short'),
  createData(2, 'XRP', '1000 XRP', 1, 1000, 5, 'Long'),
  createData(3, 'CSH', '20 LTC', 150, 1000, 15, 'Short'),
];

function handleClosePosition(id) {
  console.log(`Close position with id: ${id}`);
}

// Styling for Long and Short positions
const LongPosition = styled('span')({
  color: '#4caf50',
  fontWeight: 'bold',
});

const ShortPosition = styled('span')({
  color: '#f44336',
  fontWeight: 'bold',
});

export default function Positions() {
  const { marketData } = useMarketData();

  const calculatePNL = (entryPrice, currentPrice, size, position) => {
    const sizeNumber = parseFloat(size);
    const pnl = position === 'Long'
      ? (currentPrice - entryPrice) * sizeNumber
      : (entryPrice - currentPrice) * sizeNumber;
    return pnl.toFixed(2);
  };

  const calculateROI = (pnl, margin) => {
    const roi = (pnl / margin) * 100;
    return roi.toFixed(2);
  };

  return (
    <React.Fragment>
      <Title>Positions</Title>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Symbol</TableCell>
            <TableCell>Size</TableCell>
            <TableCell>Entry Price</TableCell>
            <TableCell>Margin</TableCell>
            <TableCell>Mark Price</TableCell>
            <TableCell>PNL</TableCell>
            <TableCell>ROI %</TableCell>
            <TableCell>Position</TableCell>
            <TableCell>Leverage</TableCell>
            <TableCell>Close Position</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => {
            const currentPrice = marketData[row.symbol] || row.entryPrice;
            const pnl = calculatePNL(row.entryPrice, currentPrice, row.size, row.position);
            const roi = calculateROI(pnl, row.margin);
            return (
              <TableRow key={row.id}>
                <TableCell>{row.symbol}</TableCell>
                <TableCell>{row.size}</TableCell>
                <TableCell>{row.entryPrice}</TableCell>
                <TableCell>{row.margin}</TableCell>
                <TableCell>{currentPrice.toFixed(2)}</TableCell>
                <TableCell>{pnl}</TableCell>
                <TableCell>{roi}</TableCell>
                <TableCell>
                  {row.position === 'Long' ? (
                    <LongPosition>Long</LongPosition>
                  ) : (
                    <ShortPosition>Short</ShortPosition>
                  )}
                </TableCell>
                <TableCell>
                  <span style={{ fontSize: '0.8rem', color: 'grey' }}>{`${row.leverage}x`}</span>
                </TableCell>
                <TableCell>
                  <Button
                    variant="contained"
                    color="secondary"
                    size="small"
                    onClick={() => handleClosePosition(row.id)}
                  >
                    Close
                  </Button>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </React.Fragment>
  );
}
