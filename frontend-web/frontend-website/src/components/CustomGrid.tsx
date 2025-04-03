import React from 'react';
import { Grid, GridProps } from '@mui/material';

// Create a type that omits the component prop from GridProps
type CustomGridItemProps = GridProps & {
  children: React.ReactNode;
  item?: boolean;
  xs?: number;
  sm?: number;
  md?: number;
  lg?: number;
};

// Create a wrapper component for Grid item
export const GridItem: React.FC<CustomGridItemProps> = (props) => {
  return <Grid {...props} />;
};

export default GridItem; 