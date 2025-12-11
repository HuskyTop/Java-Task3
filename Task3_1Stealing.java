import java.util.*;
import java.util.concurrent.*;

public class Task3_1Stealing {

    // Threshold: tasks smaller than this are processed directly
    private static final int THRESHOLD = 50;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Matrix Multiplication (Work Stealing) ===");

        // User Inputs
        System.out.print("Rows A: ");
        int rowsA = getValidInt(scanner);
        System.out.print("Cols A: ");
        int colsA = getValidInt(scanner);

        System.out.print("Rows B: ");
        int rowsB = getValidInt(scanner);
        System.out.print("Cols B: ");
        int colsB = getValidInt(scanner);

        if (colsA != rowsB) {
            System.err.println("Error: Cols A must equal Rows B.");
            return;
        }

        System.out.print("Range Min: ");
        int min = getAnyInt(scanner);

        System.out.print("Range Max: ");
        int max = getAnyInt(scanner);

        if (min > max) {
            System.out.println("Auto-swapping: Min (" + min + ") was > Max (" + max + ")");
            int temp = min;
            min = max;
            max = temp;
        }

        // Generation
        System.out.println("Generating data...");
        int[][] matrixA = generateMatrix(rowsA, colsA, min, max);
        int[][] matrixB = generateMatrix(rowsB, colsB, min, max);
        int[][] result = new int[rowsA][colsB];

        if (rowsA <= 10)
            printMatrix("A", matrixA);
        if (rowsB <= 10)
            printMatrix("B", matrixB);

        // Execution (Fork/Join)
        ForkJoinPool pool = new ForkJoinPool();
        MatrixTask task = new MatrixTask(matrixA, matrixB, result, 0, rowsA);

        long start = System.nanoTime();
        pool.invoke(task); // Start the recursive task
        long end = System.nanoTime();

        // Output
        if (rowsA <= 10)
            printMatrix("Result", result);
        System.out.printf("Time taken: %.3f ms%n", (end - start) / 1_000_000.0);
    }

    // --- Recursive Task ---
    static class MatrixTask extends RecursiveAction {
        private final int[][] A, B, C;
        private final int startRow, endRow;

        public MatrixTask(int[][] A, int[][] B, int[][] C, int start, int end) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = start;
            this.endRow = end;
        }

        @Override
        protected void compute() {
            // If task is small enough, do the work
            if ((endRow - startRow) <= THRESHOLD) {
                multiplyDirectly();
            } else {
                // Split task (Stealing logic happens here)
                int mid = (startRow + endRow) / 2;
                invokeAll(
                        new MatrixTask(A, B, C, startRow, mid),
                        new MatrixTask(A, B, C, mid, endRow));
            }
        }

        private void multiplyDirectly() {
            int colsA = A[0].length;
            int colsB = B[0].length;
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        C[i][j] += A[i][k] * B[k][j];
                    }
                }
            }
        }
    }

    // --- Helpers ---
    private static int[][] generateMatrix(int r, int c, int min, int max) {
        Random rnd = new Random();
        int[][] m = new int[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                m[i][j] = rnd.nextInt(max - min + 1) + min;
        return m;
    }

    private static int getValidInt(Scanner s) {
        while (true) {
            if (s.hasNextInt()) {
                int n = s.nextInt();
                if (n > 0) {
                    return n;
                }
                System.out.print("Value must be > 0. Try again: ");
            } else {
                System.out.print("Not a number. Try again: ");
                s.next();
            }
        }
    }

    private static int getAnyInt(Scanner s) {
        while (!s.hasNextInt()) {
            System.out.print("Not a number. Try again: ");
            s.next();
        }
        return s.nextInt();
    }

    private static void printMatrix(String name, int[][] m) {
        System.out.println("--- " + name + " ---");
        for (int[] row : m)
            System.out.println(Arrays.toString(row));
    }
}