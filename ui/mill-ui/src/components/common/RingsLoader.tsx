import { Box } from '@mantine/core';
import classes from './RingsLoader.module.css';

interface RingsLoaderProps {
  size?: number;
}

export function RingsLoader({ size = 16 }: RingsLoaderProps) {
  return (
    <Box
      component="svg"
      className={classes.loader}
      viewBox="0 0 40 40"
      style={{ width: size, height: size }}
    >
      <circle className={classes.ring1} cx="20" cy="20" r="16" />
      <circle className={classes.ring2} cx="20" cy="20" r="11" />
      <circle className={classes.ring3} cx="20" cy="20" r="6" />
    </Box>
  );
}
