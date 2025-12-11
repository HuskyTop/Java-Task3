import java.util.*;
import java.util.concurrent.*;

public class Task3_1Dealing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Matrix Multiplication (Work Dealing) ===");

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

        // Execution (Dealing)
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Future<?>> futures = new ArrayList<>();

        long start = System.nanoTime();

        // Calculate chunk size (Dealing logic)
        int chunkSize = (int) Math.ceil((double) rowsA / cores);

        for (int i = 0; i < rowsA; i += chunkSize) {
            final int startRow = i;
            final int endRow = Math.min(i + chunkSize, rowsA);

            // Submit explicit task to pool
            futures.add(executor.submit(() -> {
                multiplyRange(matrixA, matrixB, result, startRow, endRow);
            }));
        }

        // Wait for completion
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long end = System.nanoTime();
        executor.shutdown();

        // 4. Output
        if (rowsA <= 10)
            printMatrix("Result", result);
        System.out.printf("Time taken: %.3f ms%n", (end - start) / 1_000_000.0);
    }

    // Logic for partial multiplication
    private static void multiplyRange(int[][] A, int[][] B, int[][] C, int start, int end) {
        int colsA = A[0].length;
        int colsB = B[0].length;
        for (int i = start; i < end; i++) {
            for (int j = 0; j < colsB; j++) {
                int sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
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